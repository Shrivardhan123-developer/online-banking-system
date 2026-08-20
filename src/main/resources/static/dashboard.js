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
    "transactions"
];


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

                **** ****
                ${String(account.accountNumber).slice(-4)}

            </div>


            <div class="account-balance">

                ₹${formatNumber(account.balance)}

            </div>


            <div class="account-action">

                <button
                    onclick="selectAccount('${account.accountNumber}')">

                    View Transactions

                </button>

            </div>

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
                No transactions found.
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

            <div class="transaction-info">

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
    message
) {

    const element =
        document.getElementById(
            "dashboardMessage"
        );


    if (element) {

        element.textContent =
            message;
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