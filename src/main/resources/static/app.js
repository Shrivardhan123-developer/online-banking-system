const API = "/api";

const ACCOUNT_NUMBER = "2687074414";


// =====================================================
// PAGE NAVIGATION
// =====================================================

function showSection(sectionId) {

    const sections = document.querySelectorAll(".section");

    sections.forEach(section => {
        section.classList.remove("active-section");
    });

    const selectedSection =
        document.getElementById(sectionId);

    selectedSection.classList.add("active-section");


    // Update sidebar

    const navItems =
        document.querySelectorAll(".nav-item");

    navItems.forEach(item => {
        item.classList.remove("active");
    });


    // Update title

    const titles = {
        dashboard: "Dashboard",
        deposit: "Deposit Money",
        withdraw: "Withdraw Money",
        transfer: "Transfer Money",
        transactions: "Transaction History"
    };

    document.getElementById("page-title").textContent =
        titles[sectionId];


    if (sectionId === "transactions") {
        loadTransactions();
    }

    if (sectionId === "dashboard") {
        loadBalance();
        loadRecentTransactions();
    }
}


// =====================================================
// LOAD BALANCE
// =====================================================

async function loadBalance() {

    try {

        const response = await fetch(
            `${API}/accounts/${ACCOUNT_NUMBER}/balance`
        );

        if (!response.ok) {
            throw new Error("Unable to fetch balance");
        }

        const data = await response.json();

        document.getElementById("dashboardBalance")
            .textContent =
            `₹${Number(data.balance).toLocaleString("en-IN", {
                minimumFractionDigits: 2
            })}`;

    } catch (error) {

        console.error(error);

    }
}


// =====================================================
// DEPOSIT
// =====================================================

async function depositMoney() {

    const amount =
        Number(document.getElementById("depositAmount").value);

    const message =
        document.getElementById("depositMessage");


    if (!amount || amount <= 0) {

        message.textContent =
            "Please enter a valid amount.";

        message.style.color = "#dc2626";

        return;
    }


    try {

        const response = await fetch(
            `${API}/accounts/deposit`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    accountNumber: ACCOUNT_NUMBER,
                    amount: amount
                })
            }
        );


        const data = await response.json();


        if (!response.ok) {

            throw new Error(
                data.message || "Deposit failed"
            );
        }


        message.textContent =
            `✓ ${data.message}. New balance: ₹${Number(data.balance).toLocaleString("en-IN")}`;

        message.style.color = "#16a34a";


        document.getElementById("depositAmount").value = "";


        await loadBalance();

        await loadRecentTransactions();

    } catch (error) {

        message.textContent =
            `✗ ${error.message}`;

        message.style.color = "#dc2626";
    }
}


// =====================================================
// WITHDRAW
// =====================================================

async function withdrawMoney() {

    const amount =
        Number(document.getElementById("withdrawAmount").value);

    const message =
        document.getElementById("withdrawMessage");


    if (!amount || amount <= 0) {

        message.textContent =
            "Please enter a valid amount.";

        message.style.color = "#dc2626";

        return;
    }


    try {

        const response = await fetch(
            `${API}/accounts/withdraw`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    accountNumber: ACCOUNT_NUMBER,
                    amount: amount
                })
            }
        );


        const data = await response.json();


        if (!response.ok) {

            throw new Error(
                data.message || "Withdrawal failed"
            );
        }


        message.textContent =
            `✓ ${data.message}. Remaining balance: ₹${Number(data.balance).toLocaleString("en-IN")}`;

        message.style.color = "#16a34a";


        document.getElementById("withdrawAmount").value = "";


        await loadBalance();

        await loadRecentTransactions();

    } catch (error) {

        message.textContent =
            `✗ ${error.message}`;

        message.style.color = "#dc2626";
    }
}


// =====================================================
// TRANSFER
// =====================================================

async function transferMoney() {

    const receiver =
        document.getElementById("receiverAccount").value.trim();

    const amount =
        Number(document.getElementById("transferAmount").value);

    const message =
        document.getElementById("transferMessage");


    if (!receiver) {

        message.textContent =
            "Please enter receiver account number.";

        message.style.color = "#dc2626";

        return;
    }


    if (!amount || amount <= 0) {

        message.textContent =
            "Please enter a valid amount.";

        message.style.color = "#dc2626";

        return;
    }


    try {

        const url =
            `${API}/accounts/transfer` +
            `?senderAccountNumber=${encodeURIComponent(ACCOUNT_NUMBER)}` +
            `&receiverAccountNumber=${encodeURIComponent(receiver)}` +
            `&amount=${encodeURIComponent(amount)}`;


        const response = await fetch(
            url,
            {
                method: "POST"
            }
        );


        const text = await response.text();


        if (!response.ok) {

            throw new Error(
                text || "Transfer failed"
            );
        }


        message.textContent =
            `✓ ${text}`;

        message.style.color = "#16a34a";


        document.getElementById("receiverAccount").value = "";

        document.getElementById("transferAmount").value = "";


        await loadBalance();

        await loadRecentTransactions();

    } catch (error) {

        message.textContent =
            `✗ ${error.message}`;

        message.style.color = "#dc2626";
    }
}


// =====================================================
// TRANSACTION HISTORY
// =====================================================

async function loadTransactions() {

    const container =
        document.getElementById("allTransactions");


    try {

        const response = await fetch(
            `${API}/transactions/${ACCOUNT_NUMBER}`
        );


        if (!response.ok) {
            throw new Error("Unable to load transactions");
        }


        const transactions =
            await response.json();


        if (transactions.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    No transactions yet.
                </div>
            `;

            return;
        }


        container.innerHTML =
            transactions.map(createTransactionHTML).join("");


    } catch (error) {

        container.innerHTML = `
            <div class="empty-state">
                Unable to load transactions.
            </div>
        `;

        console.error(error);
    }
}


// =====================================================
// RECENT TRANSACTIONS
// =====================================================

async function loadRecentTransactions() {

    const container =
        document.getElementById("recentTransactions");


    try {

        const response = await fetch(
            `${API}/transactions/${ACCOUNT_NUMBER}`
        );


        if (!response.ok) {
            throw new Error("Unable to load transactions");
        }


        const transactions =
            await response.json();


        const recent =
            transactions.slice(0, 5);


        if (recent.length === 0) {

            container.innerHTML = `
                <div class="empty-state">
                    No transactions yet.
                </div>
            `;

            return;
        }


        container.innerHTML =
            recent.map(createTransactionHTML).join("");


    } catch (error) {

        console.error(error);
    }
}


// =====================================================
// CREATE TRANSACTION HTML
// =====================================================

function createTransactionHTML(transaction) {

    const type =
        transaction.type || "";


    let cssClass = "transfer";

    let icon = "🔄";

    let sign = "-";


    if (
        type === "DEPOSIT" ||
        type === "TRANSFER_IN"
    ) {

        cssClass = "deposit";

        icon = "💰";

        sign = "+";

    } else if (
        type === "WITHDRAW" ||
        type === "TRANSFER_OUT"
    ) {

        cssClass = "withdraw";

        icon = "💸";

        sign = "-";
    }


    const date =
        new Date(transaction.transactionDate)
            .toLocaleString("en-IN");


    return `
        <div class="transaction">

            <div class="transaction-left">

                <div class="transaction-icon">
                    ${icon}
                </div>

                <div>

                    <strong>
                        ${transaction.type}
                    </strong>

                    <small>
                        ${transaction.description || "Transaction"}
                    </small>

                    <small>
                        ${date}
                    </small>

                </div>

            </div>

            <div class="transaction-amount ${cssClass}">
                ${sign}₹${Number(transaction.amount).toLocaleString("en-IN")}
            </div>

        </div>
    `;
}


// =====================================================
// LOGOUT
// =====================================================

function logout() {

    alert("Logout functionality will be connected with Spring Security later.");
}


// =====================================================
// INITIAL LOAD
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadBalance();

        loadRecentTransactions();

    }
);