const API = "/api";


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", async () => {

    if (!isLoggedIn()) {
        window.location.href = "/login.html";
        return;
    }

    loadCustomerInfo();

    await loadDashboard();

    showSection(currentSectionFromHash());
});


// =====================================================
// CHECK LOGIN
// =====================================================

function isLoggedIn() {

    return !!localStorage.getItem("token");
}


// =====================================================
// SIDEBAR NAVIGATION
// Switches between the dashboard overview, accounts and
// transactions sections on the same page. The active
// section is persisted in the URL hash so a browser
// refresh keeps the user where they were.
// =====================================================

const SECTION_NAMES = [
    "dashboard",
    "accounts",
    "transactions",
    "profile"
];


// Cache of the authenticated customer's accounts, refreshed from
// the dashboard API after every banking operation.
let currentAccounts = [];

// Cache of the latest transaction objects for the detail modal.
let lastTransactions = [];

// Cache of the last loaded customer profile for the edit form.
let lastProfile = null;


function currentSectionFromHash() {

    const raw =
        (window.location.hash || "")
            .replace("#section-", "")
            .replace("#", "");

    return SECTION_NAMES.includes(raw)
        ? raw
        : "dashboard";
}


function showSection(name) {

    if (!SECTION_NAMES.includes(name)) {
        name = "dashboard";
    }


    // -------------------------------------------------
    // 1. Toggle the visible page section
    // -------------------------------------------------

    SECTION_NAMES.forEach(section => {

        const element =
            document.getElementById(
                "section-" + section
            );

        if (element) {

            element.classList.toggle(
                "active",
                section === name
            );
        }
    });


    // -------------------------------------------------
    // 2. Highlight the active sidebar item
    // -------------------------------------------------

    document.querySelectorAll(
        ".nav-item"
    ).forEach(button => {

        button.classList.toggle(
            "active",
            button.dataset.section === name
        );
    });


    // -------------------------------------------------
    // 3. Persist the section in the URL hash
    // -------------------------------------------------

    if (window.location.hash !== "#section-" + name) {

        window.location.hash =
            "section-" + name;
    }


    // -------------------------------------------------
    // 4. Load section-specific data
    // -------------------------------------------------

    if (name === "profile") {
        loadProfile();
    }
}


// =====================================================
// CUSTOMER INFO
// =====================================================

function loadCustomerInfo() {

    const name =
        localStorage.getItem("customerName");

    const email =
        localStorage.getItem("customerEmail");

    const nameElements =
        document.querySelectorAll(
            "[data-customer-name]"
        );

    nameElements.forEach(element => {

        element.textContent =
            name || "Customer";

    });


    const emailElements =
        document.querySelectorAll(
            "[data-customer-email]"
        );

    emailElements.forEach(element => {

        element.textContent =
            email || "";

    });
}


// =====================================================
// LOAD CUSTOMER DASHBOARD
// =====================================================

async function loadDashboard() {

    try {

        const response =
            await fetch(
                `${API}/dashboard`,
                {
                    headers: authHeaders()
                }
            );


        if (response.status === 401) {

            redirectToLogin();
            return;
        }


        if (response.status === 403) {

            showDashboardMessage(
                "Access denied. You do not have permission to view this dashboard."
            );

            return;
        }


        if (!response.ok) {

            throw new Error(
                "Unable to load dashboard data"
            );
        }


        const data =
            await response.json();


        displayDashboard(data);

    } catch (error) {

        console.error(
            "Dashboard error:",
            error
        );

        showDashboardMessage(
            "Unable to load dashboard data. Please try again."
        );
    }
}


// =====================================================
// DISPLAY CUSTOMER DATA
// =====================================================

function displayDashboard(data) {

    // =================================================
    // PROFILE / HEADER (real data from the dashboard API)
    // =================================================

    if (data.customer) {

        const nameElements =
            document.querySelectorAll(
                "[data-customer-name]"
            );

        nameElements.forEach(element => {

            element.textContent =
                data.customer.fullName ||
                "Customer";

        });


        const emailElements =
            document.querySelectorAll(
                "[data-customer-email]"
            );

        emailElements.forEach(element => {

            element.textContent =
                data.customer.email || "";

        });
    }


    // =================================================
    // TOTAL BALANCE
    // =================================================

    const totalBalanceElement =
        document.getElementById(
            "totalBalance"
        );


    if (totalBalanceElement) {

        totalBalanceElement.textContent =
            formatCurrency(data.totalBalance || 0);
    }


    // =================================================
    // ACCOUNT COUNT + ACTIVE STATUS
    // =================================================

    const accounts =
        data.accounts || [];

    // Cache so the deposit/withdraw/transfer account pickers
    // always reflect the authenticated customer's accounts.
    currentAccounts = accounts;

    const accountCountElement =
        document.getElementById(
            "accountCount"
        );


    if (accountCountElement) {

        accountCountElement.textContent =
            accounts.length;
    }


    const activeAccounts =
        accounts.filter(account =>

            account.status === "ACTIVE"

        ).length;


    const statusElement =
        document.getElementById(
            "activeAccounts"
        );


    if (statusElement) {

        statusElement.textContent =
            accounts.length === 0
                ? "No Accounts"
                : (activeAccounts === accounts.length
                    ? "All Active"
                    : `${activeAccounts} of ${accounts.length} Active`);
    }


    // =================================================
    // DISPLAY ACCOUNTS
    // =================================================

    displayAccounts(accounts);


    // =================================================
    // RECENT TRANSACTIONS (real backend summary)
    // =================================================

    displayTransactions(
        data.recentTransactions || []
    );
}


// =====================================================
// DISPLAY ACCOUNTS
// =====================================================

function displayAccounts(accounts) {

    const container =
        document.getElementById(
            "accountsContainer"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (accounts.length === 0) {

        container.innerHTML = `
            <div class="empty-state">
                No bank accounts found.
            </div>
        `;

        return;
    }


    accounts.forEach(account => {

        const card =
            document.createElement("div");


        card.className =
            "account-card";


        const isActive =
            account.status === "ACTIVE";


        card.innerHTML = `

            <div class="account-card-top">

                <span>
                    ${account.accountType || "ACCOUNT"}
                </span>

                <span class="status">
                    ${account.status || "ACTIVE"}
                </span>

            </div>


            <div class="account-number">
                ${account.accountNumber || ""}
            </div>


            <div class="account-balance">

                ₹${formatNumber(account.balance)}

            </div>


            <div class="account-actions">

                <button
                    class="btn-small ${isActive ? "" : "btn-disabled"}"
                    ${isActive ? "" : "disabled"}
                    onclick="openDepositModal('${account.accountNumber}')">
                    Deposit
                </button>

                <button
                    class="btn-small ${isActive ? "" : "btn-disabled"}"
                    ${isActive ? "" : "disabled"}
                    onclick="openWithdrawModal('${account.accountNumber}')">
                    Withdraw
                </button>

                <button
                    class="btn-small ${isActive ? "" : "btn-disabled"}"
                    ${isActive ? "" : "disabled"}
                    onclick="openTransferModal('${account.accountNumber}')">
                    Transfer
                </button>

                <button
                    class="btn-small"
                    onclick="selectAccount('${account.accountNumber}')">
                    Transactions
                </button>

            </div>

            ${isActive ? "" :
                `<div class="account-note">
                     This account is ${account.status}.
                     Operations are disabled.
                 </div>`}

        `;


        container.appendChild(card);

    });
}


// =====================================================
// SELECT ACCOUNT
// =====================================================

async function selectAccount(accountNumber) {

    localStorage.setItem(
        "selectedAccount",
        accountNumber
    );


    await loadTransactions(
        accountNumber
    );


    // Show the transactions section so the user can
    // see the freshly loaded per-account history.
    showSection("transactions");
}


// =====================================================
// LOAD TRANSACTIONS
// =====================================================

async function loadTransactions(
    accountNumber
) {

    const container =
        document.getElementById(
            "transactionsContainer"
        );


    if (!container) {
        return;
    }


    try {

        container.innerHTML = `
            <div class="loading">
                Loading transactions...
            </div>
        `;


        const response =
            await fetch(
                `${API}/transactions/${accountNumber}`,
                {
                    headers: authHeaders()
                }
            );


        if (response.status === 401) {

            redirectToLogin();
            return;
        }

        if (!response.ok) {

            throw new Error(
                "Unable to fetch transactions"
            );
        }


        const transactions =
            await response.json();

        lastTransactions =
            transactions || [];

        displayTransactions(
            transactions
        );


    } catch (error) {

        console.error(error);


        container.innerHTML = `
            <div class="empty-state">
                Unable to load transactions.
            </div>
        `;
    }
}


// =====================================================
// DISPLAY TRANSACTIONS
// =====================================================

function displayTransactions(
    transactions
) {

    const container =
        document.getElementById(
            "transactionsContainer"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (!transactions ||
        transactions.length === 0) {

        container.innerHTML = `
            <div class="empty-state">
                No transactions yet.
            </div>
        `;

        return;
    }


    transactions.forEach(transaction => {

        const row =
            document.createElement("div");


        row.className =
            "transaction-row";


        const isIncoming =
            transaction.type ===
            "DEPOSIT" ||
            transaction.type ===
            "TRANSFER_IN";


        const sign =
            isIncoming ? "+" : "-";


        row.innerHTML = `

            <div
                class="transaction-info"
                onclick="openTransactionDetail(${transaction.id})">

                <strong>
                    ${transaction.type}
                </strong>

                <span>
                    ${transaction.description || ""}
                </span>

                <small>
                    ${formatDate(
                        transaction.transactionDate
                    )}
                </small>

            </div>


            <div class="
                transaction-amount
                ${isIncoming
                    ? "credit"
                    : "debit"}
            ">

                ${sign}
                ₹${formatNumber(
                    transaction.amount
                )}

            </div>

        `;


        container.appendChild(row);

    });
}


// =====================================================
// FORMAT CURRENCY
// =====================================================

function formatCurrency(amount) {

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2
        }
    ).format(amount);

}


// =====================================================
// FORMAT NUMBER
// =====================================================

function formatNumber(amount) {

    return new Intl.NumberFormat(
        "en-IN",
        {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }
    ).format(
        Number(amount || 0)
    );
}


// =====================================================
// FORMAT DATE
// =====================================================

function formatDate(date) {

    if (!date) {
        return "";
    }


    return new Date(date)
        .toLocaleString(
            "en-IN",
            {
                dateStyle: "medium",
                timeStyle: "short"
            }
        );
}


// =====================================================
// DASHBOARD MESSAGE
// =====================================================

function showDashboardMessage(
    message,
    type
) {

    const element =
        document.getElementById(
            "dashboardMessage"
        );


    if (!element) {
        return;
    }


    element.textContent =
        message;

    // 2 = default (error red), "success" = green
    element.className =
        "dashboard-message " +
        (type === "success"
            ? "message-success"
            : "message-error");


    if (type === "success") {

        setTimeout(() => {

            element.textContent = "";
            element.className = "dashboard-message";

        }, 5000);
    }
}


// =====================================================
// AUTH HEADERS
// Sends the stored JWT with every authenticated API call.
// =====================================================

function authHeaders() {

    return {
        "Authorization":
            "Bearer " +
            (localStorage.getItem("token") || "")
    };
}


// =====================================================
// REDIRECT ON UNAUTHORIZED
// Clears local state and sends the user back to login.
// =====================================================

function redirectToLogin() {

    localStorage.clear();

    window.location.href =
        "/login.html";
}
// =====================================================
// BANKING OPERATIONS (Deposit / Withdraw / Transfer)
// All requests use the JWT Bearer token. The backend remains
// the authoritative ownership/security boundary. No customer
// or account id from the UI is ever trusted for authorization.
// =====================================================


// -----------------------------------------------------
// Helpers
// -----------------------------------------------------

function showError(id, messageText) {

    const element =
        document.getElementById(id);

    if (element) {
        element.textContent = messageText;
    }
}


function readJsonSafely(response) {

    const contentType =
        response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        return response.json().catch(() => null);
    }

    return null;
}


function messageFrom(status, data) {

    if (data && data.message) {
        return data.message;
    }

    if (status === 400) {
        return "Invalid request. Please check the details.";
    }

    if (status === 403) {
        return "Access denied. You do not have permission.";
    }

    if (status === 404) {
        return "Account not found.";
    }

    if (status === 409) {
        return "The request conflicts with the current state.";
    }

    if (status >= 500) {
        return "A server error occurred. Please try again later.";
    }

    return "The request could not be completed.";
}


function refreshDashboard() {

    loadDashboard();
}


// -----------------------------------------------------
// Modal visibility helpers
// -----------------------------------------------------

function showModal(id) {

    const overlay =
        document.getElementById(id);

    if (overlay) {
        overlay.classList.add("active");
    }
}


function hideModal(id) {

    const overlay =
        document.getElementById(id);

    if (overlay) {
        overlay.classList.remove("active");
    }
}


// -----------------------------------------------------
// Fill the account <select> controls from the cached list.
// Only ACTIVE accounts are offered for operations because
// the backend rejects deposits/withdrawals from inactive
// accounts.
// -----------------------------------------------------

function populateAccountOptions(selectId, selectedNumber) {

    const select =
        document.getElementById(selectId);

    if (!select) {
        return;
    }

    const activeAccounts =
        currentAccounts.filter(account =>
            account.status === "ACTIVE"
        );

    let options = "";

    if (activeAccounts.length === 0) {
        options =
            `<option value="">No active accounts</option>`;
    } else {

        activeAccounts.forEach(account => {

            const selected =
                account.accountNumber === selectedNumber
                    ? "selected"
                    : "";

            options +=
                `<option value="${account.accountNumber}"
                     ${selected}>
                     ${account.accountNumber}
                     (₹${formatNumber(account.balance)})
                 </option>`;
        });
    }

    select.innerHTML = options;
}
// -----------------------------------------------------
// Deposit
// -----------------------------------------------------

function openDepositModal(accountNumber) {

    populateAccountOptions("depositAccount", accountNumber);

    const amount = document.getElementById("depositAmount");
    if (amount) { amount.value = ""; }

    const description = document.getElementById("depositDescription");
    if (description) { description.value = ""; }

    showError("depositError", "");

    showModal("depositModal");
}


function closeDepositModal() {

    hideModal("depositModal");
}


async function submitDeposit() {

    const account =
        document.getElementById("depositAccount").value;

    const amountRaw =
        document.getElementById("depositAmount").value;

    const description =
        document.getElementById("depositDescription").value.trim();

    const errorEl =
        document.getElementById("depositError");

    const button =
        document.getElementById("depositSubmit");

    if (!account) {
        showError(errorEl, "Please select an account.");
        return;
    }

    if (!amountRaw ||
        isNaN(Number(amountRaw)) ||
        Number(amountRaw) <= 0) {

        showError(errorEl,
            "Please enter a valid amount greater than zero.");
        return;
    }

    if (button.disabled) {
        return;
    }

    button.disabled = true;
    button.textContent = "Depositing...";

    try {

        const response = await fetch(
            `${API}/accounts/deposit`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...authHeaders()
                },
                body: JSON.stringify({
                    accountNumber: account,
                    amount: Number(amountRaw),
                    description: description || undefined
                })
            }
        );

        const data = await readJsonSafely(response);

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            showError(errorEl, messageFrom(response.status, data));
            return;
        }

        closeDepositModal();

        showDashboardMessage(
            `${formatCurrency(Number(amountRaw))} deposited successfully.`,
            "success");

        refreshDashboard();

    } catch (error) {

        console.error(error);
        showError(errorEl, "Deposit failed. Please try again.");

    } finally {

        button.disabled = false;
        button.textContent = "Deposit Money";
    }
}
// -----------------------------------------------------
// Withdraw
// -----------------------------------------------------

function openWithdrawModal(accountNumber) {

    populateAccountOptions("withdrawAccount", accountNumber);

    const amount = document.getElementById("withdrawAmount");
    if (amount) { amount.value = ""; }

    const description = document.getElementById("withdrawDescription");
    if (description) { description.value = ""; }

    showError("withdrawError", "");

    showModal("withdrawModal");
}


function closeWithdrawModal() {

    hideModal("withdrawModal");
}


async function submitWithdraw() {

    const account =
        document.getElementById("withdrawAccount").value;

    const amountRaw =
        document.getElementById("withdrawAmount").value;

    const description =
        document.getElementById("withdrawDescription").value.trim();

    const errorEl =
        document.getElementById("withdrawError");

    const button =
        document.getElementById("withdrawSubmit");

    if (!account) {
        showError(errorEl, "Please select an account.");
        return;
    }

    if (!amountRaw ||
        isNaN(Number(amountRaw)) ||
        Number(amountRaw) <= 0) {

        showError(errorEl,
            "Please enter a valid amount greater than zero.");
        return;
    }

    if (button.disabled) {
        return;
    }

    button.disabled = true;
    button.textContent = "Withdrawing...";

    try {

        const response = await fetch(
            `${API}/accounts/withdraw`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...authHeaders()
                },
                body: JSON.stringify({
                    accountNumber: account,
                    amount: Number(amountRaw),
                    description: description || undefined
                })
            }
        );

        const data = await readJsonSafely(response);

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            showError(errorEl, messageFrom(response.status, data));
            return;
        }

        closeWithdrawModal();

        showDashboardMessage(
            `Withdrew ${formatCurrency(Number(amountRaw))} successfully.`,
            "success");

        refreshDashboard();

    } catch (error) {

        console.error(error);
        showError(errorEl, "Withdrawal failed. Please try again.");

    } finally {

        button.disabled = false;
        button.textContent = "Withdraw Money";
    }
}
// -----------------------------------------------------
// Transfer
// -----------------------------------------------------

function openTransferModal(accountNumber) {

    populateAccountOptions("transferFrom", accountNumber);

    const to = document.getElementById("transferTo");
    if (to) { to.value = ""; }

    const amount = document.getElementById("transferAmount");
    if (amount) { amount.value = ""; }

    const description = document.getElementById("transferDescription");
    if (description) { description.value = ""; }

    showError("transferError", "");

    showModal("transferModal");
}


function closeTransferModal() {

    hideModal("transferModal");
}


async function submitTransfer() {

    const from =
        document.getElementById("transferFrom").value.trim();

    const to =
        document.getElementById("transferTo").value.trim();

    const amountRaw =
        document.getElementById("transferAmount").value;

    const description =
        document.getElementById("transferDescription").value.trim();

    const errorEl =
        document.getElementById("transferError");

    const button =
        document.getElementById("transferSubmit");

    if (!from) {
        showError(errorEl, "Please select a source account.");
        return;
    }

    if (!to) {
        showError(errorEl, "Receiver account number is required.");
        return;
    }

    if (from === to) {
        showError(errorEl,
            "Source and destination accounts cannot be the same.");
        return;
    }

    if (!amountRaw ||
        isNaN(Number(amountRaw)) ||
        Number(amountRaw) <= 0) {

        showError(errorEl,
            "Please enter a valid amount greater than zero.");
        return;
    }

    if (button.disabled) {
        return;
    }

    button.disabled = true;
    button.textContent = "Transferring...";

    try {

        const response = await fetch(
            `${API}/transfers`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...authHeaders()
                },
                body: JSON.stringify({
                    senderAccountNumber: from,
                    receiverAccountNumber: to,
                    amount: Number(amountRaw),
                    description: description || undefined
                })
            }
        );

        const data = await readJsonSafely(response);

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            showError(errorEl, messageFrom(response.status, data));
            return;
        }

        closeTransferModal();

        showDashboardMessage(
            `${formatCurrency(Number(amountRaw))} transferred successfully.`,
            "success");

        refreshDashboard();

    } catch (error) {

        console.error(error);
        showError(errorEl, "Transfer failed. Please try again.");

    } finally {

        button.disabled = false;
        button.textContent = "Transfer Money";
    }
}
// =====================================================
// TRANSACTION DETAIL MODAL
// =====================================================

function openTransactionDetail(transactionId) {

    const transaction =
        lastTransactions.find(tx =>
            String(tx.id) === String(transactionId)
        );

    if (!transaction) {
        return;
    }

    const body =
        document.getElementById(
            "transactionDetailBody"
        );

    if (!body) {
        return;
    }

    const isIncoming =
        transaction.type === "DEPOSIT" ||
        transaction.type === "TRANSFER_IN";

    body.innerHTML = `

        <div class="detail-line">
            <span>Reference</span>
            <strong>${transaction.transactionReference || "-"}</strong>
        </div>

        <div class="detail-line">
            <span>Date</span>
            <strong>${formatDate(transaction.transactionDate)}</strong>
        </div>

        <div class="detail-line">
            <span>Type</span>
            <strong>${transaction.type || "-"}</strong>
        </div>

        <div class="detail-line">
            <span>Amount</span>
            <strong class="${isIncoming ? "credit" : "debit"}">
                ${isIncoming ? "+" : "-"}₹${formatNumber(transaction.amount)}
            </strong>
        </div>

        <div class="detail-line">
            <span>Status</span>
            <strong>${transaction.status || "-"}</strong>
        </div>

        <div class="detail-line">
            <span>Account</span>
            <strong>${transaction.accountNumber || "-"}</strong>
        </div>

        <div class="detail-line">
            <span>Source</span>
            <strong>${transaction.sourceAccount || "-"}</strong>
        </div>

        <div class="detail-line">
            <span>Destination</span>
            <strong>${transaction.destinationAccount || "-"}</strong>
        </div>

        <div class="detail-line">
            <span>Balance After</span>
            <strong>₹${formatNumber(transaction.balanceAfterTransaction)}</strong>
        </div>

        <div class="detail-line">
            <span>Description</span>
            <strong>${transaction.description || "-"}</strong>
        </div>
    `;

    showModal("transactionModal");
}


function closeTransactionModal() {

    hideModal("transactionModal");
}
// =====================================================
// PROFILE
// =====================================================

async function loadProfile() {

    const container =
        document.getElementById("profileContainer");

    if (!container) {
        return;
    }

    container.innerHTML =
        '<div class="loading">Loading profile...</div>';

    try {

        const response =
            await fetch(
                `${API}/customers/me`,
                {
                    headers: authHeaders()
                }
            );

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load profile");
        }

        const customer =
            await response.json();

        lastProfile = customer;

        renderProfile(customer);

    } catch (error) {

        console.error(error);

        container.innerHTML =
            '<div class="empty-state">Unable to load profile.</div>';
    }
}


function renderProfile(customer) {

    const container =
        document.getElementById("profileContainer");

    if (!container) {
        return;
    }

    container.innerHTML = `

        <div class="profile-grid">

            <div class="profile-card">

                <h3>Personal Details</h3>

                <div class="detail-line">
                    <span>Full Name</span>
                    <strong>${escapeHtml(customer.fullName || "")}</strong>
                </div>

                <div class="detail-line">
                    <span>Email</span>
                    <strong>${escapeHtml(customer.email || "")}</strong>
                </div>

                <div class="detail-line">
                    <span>Phone</span>
                    <strong>${escapeHtml(customer.phone || "-")}</strong>
                </div>

                <div class="detail-line">
                    <span>Role</span>
                    <strong>${customer.role || ""}</strong>
                </div>

                <button
                    class="btn-primary"
                    onclick="openEditProfile()">
                    Edit Profile
                </button>

            </div>


            <div class="profile-card">

                <h3>Security</h3>

                <p class="profile-hint">
                    Change your account password. You will be
                    redirected to log in again after a successful
                    change.
                </p>

                <button
                    class="btn-primary"
                    onclick="openPasswordModal()">
                    Change Password
                </button>

            </div>

        </div>
    `;
}


function escapeHtml(value) {

    return String(value == null ? "" : value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}
// =====================================================
// CHANGE PASSWORD
// =====================================================

function openPasswordModal() {

    const fields = [
        "currentPassword",
        "newPassword",
        "confirmNewPassword"
    ];

    fields.forEach(id => {
        const el = document.getElementById(id);
        if (el) { el.value = ""; }
    });

    showError("passwordError", "");

    showModal("passwordModal");
}


function closePasswordModal() {

    hideModal("passwordModal");
}


async function submitPasswordChange() {

    const current =
        document.getElementById("currentPassword").value;

    const newPassword =
        document.getElementById("newPassword").value;

    const confirm =
        document.getElementById("confirmNewPassword").value;

    const errorEl =
        document.getElementById("passwordError");

    const button =
        document.getElementById("passwordSubmit");

    if (!current) {
        showError(errorEl, "Current password is required.");
        return;
    }

    if (!newPassword || newPassword.length < 6) {
        showError(errorEl,
            "New password must be at least 6 characters.");
        return;
    }

    if (newPassword !== confirm) {
        showError(errorEl,
            "New password and confirmation do not match.");
        return;
    }

    if (button.disabled) {
        return;
    }

    button.disabled = true;
    button.textContent = "Updating...";

    try {

        const response =
            await fetch(
                `${API}/customers/me/password`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        ...authHeaders()
                    },
                    body: JSON.stringify({
                        currentPassword: current,
                        newPassword: newPassword,
                        confirmPassword: confirm
                    })
                }
            );

        const data = await readJsonSafely(response);

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            showError(errorEl, messageFrom(response.status, data));
            return;
        }

        // Successful password change invalidates the current token.
        localStorage.clear();

        showDashboardMessage(
            "Password changed successfully. Please log in again.",
            "success");

        setTimeout(() => {
            window.location.href = "/login.html";
        }, 1200);

    } catch (error) {

        console.error(error);
        showError(errorEl,
            "Password change failed. Please try again.");

    } finally {

        button.disabled = false;
        button.textContent = "Update Password";
    }
}


// =====================================================
// EDIT PROFILE
// =====================================================

function openEditProfile() {

    const customer = lastProfile || {};

    const fullName =
        document.getElementById("editFullName");

    const email =
        document.getElementById("editEmail");

    const phone =
        document.getElementById("editPhone");

    if (fullName) {
        fullName.value = customer.fullName || "";
    }

    if (email) {
        email.value = customer.email || "";
    }

    if (phone) {
        phone.value = customer.phone || "";
    }

    showError("editProfileError", "");

    showModal("editProfileModal");
}


function closeEditProfileModal() {

    hideModal("editProfileModal");
}


async function submitProfileUpdate() {

    const fullName =
        document.getElementById("editFullName").value.trim();

    const email =
        document.getElementById("editEmail").value.trim();

    const phone =
        document.getElementById("editPhone").value.trim();

    const errorEl =
        document.getElementById("editProfileError");

    const button =
        document.getElementById("editProfileSubmit");

    if (!fullName) {
        showError(errorEl, "Full name is required.");
        return;
    }

    if (!phone) {
        showError(errorEl, "Phone number is required.");
        return;
    }

    if (button.disabled) {
        return;
    }

    button.disabled = true;
    button.textContent = "Saving...";

    try {

        const response =
            await fetch(
                `${API}/customers/me`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        ...authHeaders()
                    },
                    body: JSON.stringify({
                        fullName: fullName,
                        email: email || undefined,
                        phone: phone
                    })
                }
            );

        const data = await readJsonSafely(response);

        if (response.status === 401) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            showError(errorEl, messageFrom(response.status, data));
            return;
        }

        closeEditProfileModal();

        localStorage.setItem("customerName", fullName);

        showDashboardMessage(
            "Profile updated successfully.",
            "success");

        loadProfile();
        loadCustomerInfo();

    } catch (error) {

        console.error(error);
        showError(errorEl,
            "Profile update failed. Please try again.");

    } finally {

        button.disabled = false;
        button.textContent = "Save Changes";
    }
}