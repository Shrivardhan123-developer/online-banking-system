const API = "/api";


// =====================================================
// PAGE STATE
// =====================================================

let customerPage = 0;
let accountPage = 0;
let auditPage = 0;

let lastViewedCustomerId = null;
let lastViewedAccount = null;


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", async () => {

    if (!isLoggedIn()) {
        window.location.href = "/login.html";
        return;
    }

    await verifyAdmin();
});


// =====================================================
// CHECK LOGIN
// =====================================================

function isLoggedIn() {

    return !!localStorage.getItem("token");
}


// =====================================================
// VERIFY ADMIN ROLE
// Frontend pre-check only. The backend /api/admin/**
// rules remain the real security boundary.
// =====================================================

async function verifyAdmin() {

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

        if (!response.ok) {
            throw new Error(
                "Unable to verify admin access"
            );
        }

        const data =
            await response.json();

        if (!data.customer ||
            data.customer.role !== "ADMIN") {

            showMessage(
                "Access denied. This area is reserved for administrators.",
                "error"
            );

            setTimeout(() => {
                window.location.href = "/dashboard.html";
            }, 2500);

            return;
        }

        document.querySelectorAll(
            "[data-admin-name]"
        ).forEach(element => {

            element.textContent =
                data.customer.fullName ||
                "Administrator";
        });

        document.querySelectorAll(
            "[data-admin-email]"
        ).forEach(element => {

            element.textContent =
                data.customer.email || "";
        });

        showSection("overview");

    } catch (error) {

        console.error(
            "Admin verification error:",
            error
        );

        showMessage(
            "Unable to load the admin console. Please try again.",
            "error"
        );
    }
}


// =====================================================
// SECTION NAVIGATION
// =====================================================

function showSection(name) {

    document.querySelectorAll(
        ".admin-section"
    ).forEach(section => {

        section.classList.remove("active");
    });

    const target =
        document.getElementById(
            "section-" + name
        );

    if (target) {
        target.classList.add("active");
    }

    document.querySelectorAll(
        ".nav-item"
    ).forEach(button => {

        button.classList.toggle(
            "active",
            button.dataset.section === name
        );
    });

    if (name === "overview") {
        loadStats();
    }

    if (name === "customers") {
        loadCustomers(customerPage);
    }

    if (name === "accounts") {
        loadAccounts(accountPage);
    }

    if (name === "audit") {
        loadAuditLogs(auditPage);
    }
}


// =====================================================
// SHARED API CALL
// =====================================================

async function apiFetch(url, options) {

    let response;

    try {

        response = await fetch(
            `${API}${url}`,
            {
                method:
                    (options && options.method) ||
                    "GET",

                headers: {
                    "Content-Type":
                        "application/json",
                    ...authHeaders()
                },

                body:
                    (options && options.body) ||
                    undefined
            }
        );

    } catch (networkError) {

        throw {
            status: 0,
            message:
                "Network error. Please check your connection."
        };
    }

    let body = null;

    const contentType =
        response.headers.get(
            "content-type"
        ) || "";

    if (contentType.includes(
            "application/json"
        )) {

        body = await response.json()
            .catch(() => null);
    }

    if (response.status === 401) {

        redirectToLogin();

        throw {
            status: 401,
            message:
                "Session expired. Please log in again."
        };
    }

    if (!response.ok) {

        const message =
            (body && body.message) ||
            defaultMessageFor(
                response.status
            );

        throw {
            status: response.status,
            message
        };
    }

    return body;
}


function defaultMessageFor(status) {

    if (status === 403) {
        return "Access denied. You do not have permission to perform this action.";
    }

    if (status === 404) {
        return "The requested resource was not found.";
    }

    if (status === 409) {
        return "The request conflicts with the current state of the resource.";
    }

    if (status >= 500) {
        return "An unexpected server error occurred. Please try again later.";
    }

    return "The request failed. Please try again.";
}


function handleApiError(error) {

    console.error(
        "Admin API error:",
        error
    );

    showMessage(
        error.message ||
        "Something went wrong.",
        "error"
    );
}
// =====================================================
// STATISTICS
// =====================================================

async function loadStats() {

    const extra =
        document.getElementById(
            "overviewExtra"
        );

    const cards =
        document.getElementById(
            "statCards"
        );

    if (!cards || !extra) {
        return;
    }

    try {

        const data =
            await apiFetch("/admin/stats");

        const statCards = [

            {
                label: "Total Customers",
                value: data.totalCustomers,
                hint:
                    data.activeCustomers + " active"
            },

            {
                label: "Total Accounts",
                value: data.totalAccounts,
                hint:
                    data.activeAccounts + " active"
            },

            {
                label: "Total Balance",
                value: formatCurrency(
                    data.totalBalance
                )
            },

            {
                label: "Total Transactions",
                value: data.totalTransactions,
                hint:
                    data.todayDeposits +
                    " deposits today"
            }
        ];

        cards.innerHTML = "";

        statCards.forEach(stat => {

            const card =
                document.createElement("div");

            card.className = "stat-card";

            card.innerHTML = `
                <span>${escapeHtml(stat.label)}</span>
                <h2>${stat.value}</h2>
                <p>${stat.hint || ""}</p>
            `;

            cards.appendChild(card);
        });

        extra.innerHTML = `
            <div class="section-header">
                <h2>Detailed Snapshot</h2>
            </div>
            <div class="detail-grid">
                <div>
                    <div class="label">Frozen Customers</div>
                    <div class="value">${data.frozenCustomers}</div>
                </div>
                <div>
                    <div class="label">Frozen Accounts</div>
                    <div class="value">${data.frozenAccounts}</div>
                </div>
                <div>
                    <div class="label">Total Deposits</div>
                    <div class="value">${data.totalDeposits}</div>
                </div>
                <div>
                    <div class="label">Total Withdrawals</div>
                    <div class="value">${data.totalWithdrawals}</div>
                </div>
                <div>
                    <div class="label">Total Transfers</div>
                    <div class="value">${data.totalTransfers}</div>
                </div>
                <div>
                    <div class="label">Today's Transfers</div>
                    <div class="value">${data.todayTransfers}</div>
                </div>
                <div>
                    <div class="label">Today's Deposit Amount</div>
                    <div class="value">${formatCurrency(data.todayDepositAmount)}</div>
                </div>
            </div>
        `;

    } catch (error) {

        handleApiError(error);
    }
}
// =====================================================
// CUSTOMERS
// =====================================================

async function loadCustomers(page) {

    customerPage = page;

    const container =
        document.getElementById(
            "customersContainer"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        '<div class="loading">Loading customers...</div>';

    try {

        const search =
            document.getElementById(
                "customerSearch"
            ).value.trim();

        const params =
            new URLSearchParams({
                page: String(page),
                size: "20"
            });

        if (search) {
            params.set("search", search);
        }

        const data =
            await apiFetch(
                `/admin/customers?${params.toString()}`
            );

        container.innerHTML = "";

        if (!data.content ||
            data.content.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    No customers found.
                </div>`;

            return;
        }

        const table =
            document.createElement("table");

        table.className = "admin-table";

        table.innerHTML = `
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Accounts</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody></tbody>
        `;

        const tbody =
            table.querySelector("tbody");

        data.content.forEach(customer => {

            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>${customer.id}</td>
                <td>${escapeHtml(customer.fullName)}</td>
                <td>${escapeHtml(customer.email)}</td>
                <td>${escapeHtml(customer.phone || "-")}</td>
                <td>${customer.role}</td>
                <td>${statusBadge(customer.status)}</td>
                <td>${(customer.accounts || []).length}</td>
                <td>
                    <button class="btn btn-outline"
                            onclick="viewCustomer(${customer.id})">
                        View
                    </button>
                    ${customer.role === "ADMIN"
                        ? ""
                        : customer.status === "ACTIVE"
                            ? `<button class="btn btn-danger"
                                 onclick="setCustomerStatus(${customer.id}, 'SUSPENDED', this)">
                                 Suspend
                               </button>`
                            : `<button class="btn btn-success"
                                 onclick="setCustomerStatus(${customer.id}, 'ACTIVE', this)">
                                 Activate
                               </button>`}
                </td>
            `;

            tbody.appendChild(row);
        });

        container.appendChild(table);

        container.appendChild(
            paginationBar(data, "loadCustomers")
        );

    } catch (error) {

        handleApiError(error);
    }
}
// =====================================================
// CUSTOMER DETAILS
// =====================================================

async function viewCustomer(id) {

    lastViewedCustomerId = id;

    const modalTitle =
        document.getElementById("adminDetailModalTitle");

    const modalBody =
        document.getElementById("adminDetailModalBody");

    if (!modalTitle || !modalBody) {
        showMessage("Unable to open the detail view.", "error");
        return;
    }

    modalTitle.textContent = "Customer Details";

    modalBody.innerHTML =
        '<div class="loading">Loading customer details...</div>';

    openAdminDetailModal();

    try {

        const customer =
            await apiFetch(
                `/admin/customers/${id}`
            );

        const accounts = customer.accounts || [];

        const accountsRows =
            accounts.map(account => `
                <tr>
                    <td>${account.accountNumber}</td>
                    <td>${account.accountType}</td>
                    <td>${formatCurrency(account.balance)}</td>
                    <td>${statusBadge(account.status)}</td>
                    <td>
                        <button class="btn btn-outline"
                                onclick="viewAccountFromCustomer('${account.accountNumber}')">
                            Transactions
                        </button>
                    </td>
                </tr>`)
                .join("");

        modalBody.innerHTML = `

            <div class="detail-line">
                <span>Customer ID</span>
                <strong>${customer.id}</strong>
            </div>

            <div class="detail-line">
                <span>Full Name</span>
                <strong>${escapeHtml(customer.fullName)}</strong>
            </div>

            <div class="detail-line">
                <span>Email</span>
                <strong>${escapeHtml(customer.email)}</strong>
            </div>

            <div class="detail-line">
                <span>Phone</span>
                <strong>${escapeHtml(customer.phone || "-")}</strong>
            </div>

            <div class="detail-line">
                <span>Role</span>
                <strong>${customer.role}</strong>
            </div>

            <div class="detail-line">
                <span>Status</span>
                <strong>${statusBadge(customer.status)}</strong>
            </div>

            <div class="detail-line">
                <span>Number of Accounts</span>
                <strong>${accounts.length}</strong>
            </div>

            ${accounts.length > 0 ? `
                <div class="detail-section-title">Accounts</div>
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Account Number</th>
                            <th>Type</th>
                            <th>Balance</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>${accountsRows}</tbody>
                </table>` : ""}
        `;

    } catch (error) {

        handleApiError(error);

        closeAdminDetailModal();
    }
}


// =====================================================
// CUSTOMER STATUS MANAGEMENT
// =====================================================

async function setCustomerStatus(id, status, button) {

    if (button) {
        button.disabled = true;
    }

    try {

        await apiFetch(
            `/admin/customers/${id}/status`,
            {
                method: "PUT",
                body: JSON.stringify({ status })
            }
        );

        await loadCustomers(customerPage);

        if (lastViewedCustomerId === id) {
            await viewCustomer(id);
        }

        showMessage(
            "Customer status updated successfully.",
            "success"
        );

    } catch (error) {

        handleApiError(error);

    } finally {

        if (button) {
            button.disabled = false;
        }
    }
}


function viewAccountFromCustomer(accountNumber) {

    showSection("accounts");

    setTimeout(() => {
        viewAccount(accountNumber);
    }, 50);
}
// =====================================================
// ACCOUNTS
// =====================================================

async function loadAccounts(page) {

    accountPage = page;

    const container =
        document.getElementById(
            "accountsContainer"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        '<div class="loading">Loading accounts...</div>';

    try {

        const params =
            new URLSearchParams({
                page: String(page),
                size: "20"
            });

        const accountNumber =
            document.getElementById(
                "accountSearch"
            ).value.trim();

        const accountType =
            document.getElementById(
                "accountTypeFilter"
            ).value;

        const status =
            document.getElementById(
                "accountStatusFilter"
            ).value;

        if (accountNumber) {
            params.set("accountNumber", accountNumber);
        }

        if (accountType) {
            params.set("accountType", accountType);
        }

        if (status) {
            params.set("status", status);
        }

        const data =
            await apiFetch(
                `/admin/accounts?${params.toString()}`
            );

        container.innerHTML = "";

        if (!data.content ||
            data.content.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    No accounts found.
                </div>`;

            return;
        }

        const table =
            document.createElement("table");

        table.className = "admin-table";

        table.innerHTML = `
            <thead>
                <tr>
                    <th>Account Number</th>
                    <th>Type</th>
                    <th>Balance</th>
                    <th>Status</th>
                    <th>Customer</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody></tbody>
        `;

        const tbody =
            table.querySelector("tbody");

        data.content.forEach(account => {

            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>${account.accountNumber}</td>
                <td>${account.accountType}</td>
                <td>${formatCurrency(account.balance)}</td>
                <td>${statusBadge(account.status)}</td>
                <td>
                    ${escapeHtml(account.customerName || "-")}
                    <br>
                    <small>${escapeHtml(account.customerEmail || "")}</small>
                </td>
                <td>
                    <button class="btn btn-outline"
                            onclick="viewAccount('${account.accountNumber}')">
                        View
                    </button>
                    ${account.status === "ACTIVE"
                        ? `<button class="btn btn-danger"
                             onclick="setAccountStatus(${account.id}, 'INACTIVE', this)">
                             Deactivate
                           </button>`
                        : `<button class="btn btn-success"
                             onclick="setAccountStatus(${account.id}, 'ACTIVE', this)">
                             Activate
                           </button>`}
                </td>
            `;

            tbody.appendChild(row);
        });

        container.appendChild(table);

        container.appendChild(
            paginationBar(data, "loadAccounts")
        );

    } catch (error) {

        handleApiError(error);
    }
}
// =====================================================
// ADMIN DETAIL MODAL HELPERS
// =====================================================

function openAdminDetailModal() {

    const overlay =
        document.getElementById("adminDetailModal");

    if (overlay) {
        overlay.classList.add("active");
    }
}


function closeAdminDetailModal() {

    const overlay =
        document.getElementById("adminDetailModal");

    if (overlay) {
        overlay.classList.remove("active");
    }
}


// Close on backdrop click
document.addEventListener("click", event => {

    if (event.target &&
        event.target.id === "adminDetailModal") {
        closeAdminDetailModal();
    }
});


// Close on Escape
document.addEventListener("keydown", event => {

    if (event.key === "Escape") {
        closeAdminDetailModal();
    }
});


// =====================================================
// ACCOUNT DETAILS + TRANSACTION MONITORING
// =====================================================

async function viewAccount(accountNumber) {

    lastViewedAccount = accountNumber;

    const modalTitle =
        document.getElementById("adminDetailModalTitle");

    const modalBody =
        document.getElementById("adminDetailModalBody");

    if (!modalTitle || !modalBody) {
        showMessage("Unable to open the account detail view.", "error");
        return;
    }

    modalTitle.textContent = "Account Details";

    modalBody.innerHTML =
        '<div class="loading">Loading account details...</div>';

    openAdminDetailModal();

    try {

        const account =
            await apiFetch(
                `/admin/accounts/${accountNumber}`
            );

        modalBody.innerHTML = `

            <div class="detail-line">
                <span>Account Number</span>
                <strong>${account.accountNumber}</strong>
            </div>

            <div class="detail-line">
                <span>Type</span>
                <strong>${account.accountType}</strong>
            </div>

            <div class="detail-line">
                <span>Balance</span>
                <strong>${formatCurrency(account.balance)}</strong>
            </div>

            <div class="detail-line">
                <span>Status</span>
                <strong>${statusBadge(account.status)}</strong>
            </div>

            <div class="detail-line">
                <span>Owner</span>
                <strong>${escapeHtml(account.customerName || "-")}</strong>
            </div>

            <div class="detail-line">
                <span>Owner Email</span>
                <strong>${escapeHtml(account.customerEmail || "-")}</strong>
            </div>

            <div class="detail-section-title">Recent Transactions</div>

            <div id="adminAccountTxContainer"></div>
        `;

        await loadAccountTransactions(accountNumber, 0);

    } catch (error) {

        handleApiError(error);

        closeAdminDetailModal();
    }
}


// =====================================================
// ACCOUNT TRANSACTIONS
// =====================================================

async function loadAccountTransactions(
    accountNumber,
    page
) {

    const container =
        document.getElementById(
            "adminAccountTxContainer"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        '<div class="loading">Loading transactions...</div>';

    try {

        const params =
            new URLSearchParams({
                page: String(page),
                size: "10"
            });

        const data =
            await apiFetch(
                `/admin/accounts/${accountNumber}/transactions?${params.toString()}`
            );

        if (!data.content ||
            data.content.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    No transactions found for this account.
                </div>`;

            return;
        }

        const rows =
            data.content.map(tx => `
                <tr>
                    <td><small>${formatDate(tx.transactionDate)}</small></td>
                    <td>${tx.type}</td>
                    <td>${tx.status}</td>
                    <td>${formatCurrency(tx.amount)}</td>
                    <td>${tx.balanceAfterTransaction === null
                        ? "-"
                        : formatCurrency(tx.balanceAfterTransaction)}</td>
                    <td>${escapeHtml(tx.description || "-")}</td>
                    <td>
                        <button class="btn btn-outline"
                                onclick="viewTransactionDetails(${tx.id}, '${accountNumber}')">
                            View
                        </button>
                    </td>
                </tr>`)
                .join("");

        container.innerHTML = `
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th>Amount</th>
                        <th>Balance After</th>
                        <th>Description</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        `;

        container.appendChild(
            txPaginationBar(
                data,
                accountNumber
            )
        );

    } catch (error) {

        handleApiError(error);
    }
}


// =====================================================
// TRANSACTION DETAIL MODAL (admin)
// =====================================================

function viewTransactionDetails(transactionId, accountNumber) {

    const modalTitle =
        document.getElementById("adminDetailModalTitle");

    const modalBody =
        document.getElementById("adminDetailModalBody");

    if (!modalTitle || !modalBody) {
        return;
    }

    modalTitle.textContent = "Transaction Details";

    modalBody.innerHTML =
        '<div class="loading">Loading transaction details...</div>';

    openAdminDetailModal();

    apiFetch(
        `/admin/accounts/${accountNumber}/transactions?page=0&size=50`
    ).then(data => {

        const transaction =
            (data.content || []).find(tx =>
                String(tx.id) === String(transactionId)
            );

        if (!transaction) {
            modalBody.innerHTML =
                '<div class="empty-state">Transaction not found.</div>';
            return;
        }

        const isIncoming =
            transaction.type === "DEPOSIT" ||
            transaction.type === "TRANSFER_IN";

        modalBody.innerHTML = `

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
                <strong class="${isIncoming ? "badge-active" : "badge-suspended"}">
                    ${isIncoming ? "+" : "-"}${formatCurrency(transaction.amount)}
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
                <strong>${transaction.balanceAfterTransaction === null
                    ? "-"
                    : formatCurrency(transaction.balanceAfterTransaction)}</strong>
            </div>

            <div class="detail-line">
                <span>Description</span>
                <strong>${escapeHtml(transaction.description || "-")}</strong>
            </div>
        `;

    }).catch(error => {

        handleApiError(error);

        closeAdminDetailModal();
    });
}


// =====================================================
// ACCOUNT STATUS MANAGEMENT
// =====================================================

async function setAccountStatus(id, status, button) {

    if (button) {
        button.disabled = true;
    }

    try {

        await apiFetch(
            `/admin/accounts/${id}/status`,
            {
                method: "PUT",
                body: JSON.stringify({ status })
            }
        );

        await loadAccounts(accountPage);

        if (lastViewedAccount) {
            await viewAccount(
                lastViewedAccount
            );
        }

        showMessage(
            "Account status updated successfully.",
            "success"
        );

    } catch (error) {

        handleApiError(error);

    } finally {

        if (button) {
            button.disabled = false;
        }
    }
}
// =====================================================
// AUDIT LOGS
// =====================================================

async function loadAuditLogs(page) {

    auditPage = page;

    const container =
        document.getElementById(
            "auditContainer"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        '<div class="loading">Loading audit logs...</div>';

    try {

        const params =
            new URLSearchParams({
                page: String(page),
                size: "50"
            });

        const action =
            document.getElementById(
                "auditAction"
            ).value.trim();

        const username =
            document.getElementById(
                "auditUsername"
            ).value.trim();

        if (action) {
            params.set("action", action);
        }

        if (username) {
            params.set("username", username);
        }

        const data =
            await apiFetch(
                `/admin/audit-logs?${params.toString()}`
            );

        container.innerHTML = "";

        if (!data.content ||
            data.content.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    No audit log entries found.
                </div>`;

            return;
        }

        const table =
            document.createElement("table");

        table.className = "admin-table";

        table.innerHTML = `
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Action</th>
                    <th>User</th>
                    <th>Description</th>
                    <th>Created At</th>
                </tr>
            </thead>
            <tbody></tbody>
        `;

        const tbody =
            table.querySelector("tbody");

        data.content.forEach(entry => {

            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>${entry.id}</td>
                <td>${escapeHtml(entry.action)}</td>
                <td>${escapeHtml(entry.username || "-")}</td>
                <td>${escapeHtml(entry.description || "")}</td>
                <td>${formatDate(entry.createdAt)}</td>
            `;

            tbody.appendChild(row);
        });

        container.appendChild(table);

        container.appendChild(
            paginationBar(data, "loadAuditLogs")
        );
    } catch (error) {

        handleApiError(error);
    }
}
// =====================================================
// PAGINATION
// =====================================================

function paginationBar(pageData, loadFn) {

    const div =
        document.createElement("div");

    if (!pageData ||
        pageData.totalPages <= 1) {

        return div;
    }

    div.className = "pagination";

    div.innerHTML = `
        <button class="btn btn-outline"
                ${pageData.page === 0 ? "disabled" : ""}
                onclick="${loadFn}(${pageData.page - 1})">
            Previous
        </button>

        <span class="info">
            Page ${pageData.page + 1} of ${pageData.totalPages}
            (${pageData.totalElements} total)
        </span>

        <button class="btn btn-outline"
                ${pageData.last ? "disabled" : ""}
                onclick="${loadFn}(${pageData.page + 1})">
            Next
        </button>
    `;

    return div;
}


// Pagination for account transactions. The account number is
// inlined as a literal because it is a safe numeric string.
function txPaginationBar(pageData, accountNumber) {

    const div =
        document.createElement("div");

    if (!pageData ||
        pageData.totalPages <= 1) {

        return div;
    }

    div.className = "pagination";

    div.innerHTML = `
        <button class="btn btn-outline"
                ${pageData.page === 0 ? "disabled" : ""}
                onclick="loadAccountTransactions('${accountNumber}', ${pageData.page - 1})">
            Previous
        </button>

        <span class="info">
            Page ${pageData.page + 1} of ${pageData.totalPages}
            (${pageData.totalElements} total)
        </span>

        <button class="btn btn-outline"
                ${pageData.last ? "disabled" : ""}
                onclick="loadAccountTransactions('${accountNumber}', ${pageData.page + 1})">
            Next
        </button>
    `;

    return div;
}


// =====================================================
// RENDER HELPERS
// =====================================================

function statusBadge(status) {

    const label =
        (status || "").toUpperCase();

    const cssClass =

        label === "ACTIVE"
            ? "badge-active"

            : label === "SUSPENDED"
                ? "badge-suspended"

                : "badge-inactive";

    return `<span class="badge ${cssClass}">${label || "-"}</span>`;
}


function escapeHtml(value) {

    return String(
        value == null ? "" : value
    )
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}


function formatCurrency(amount) {

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2
        }
    ).format(Number(amount || 0));
}


function formatDate(date) {

    if (!date) {
        return "";
    }

    return new Date(date).toLocaleString(
        "en-IN",
        {
            dateStyle: "medium",
            timeStyle: "short"
        }
    );
}


// =====================================================
// MESSAGE
// =====================================================

function showMessage(message, type) {

    const element =
        document.getElementById(
            "adminMessage"
        );

    if (!element) {
        return;
    }

    element.textContent = message;

    element.className =
        "message-bar " +
        (type || "info");
}


// =====================================================
// AUTH HEADERS
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
// =====================================================

function redirectToLogin() {

    localStorage.clear();

    window.location.href =
        "/login.html";
}