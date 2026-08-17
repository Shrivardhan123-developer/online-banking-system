const API = "/api";

// =====================================================
// LOGIN
// =====================================================

async function login() {

    const email =
        document.getElementById("email")
            .value.trim();

    const password =
        document.getElementById("password")
            .value;

    const message =
        document.getElementById("loginMessage");

    const button =
        document.getElementById("loginButton");


    // =====================================================
    // VALIDATION
    // =====================================================

    if (!email) {

        showMessage(
            "Please enter your email.",
            false
        );

        return;
    }


    if (!password) {

        showMessage(
            "Please enter your password.",
            false
        );

        return;
    }


    // =====================================================
    // LOGIN REQUEST
    // =====================================================

    try {

        button.disabled = true;

        button.textContent =
            "Logging in...";


        const response =
            await fetch(
                `${API}/auth/login`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        email: email,

                        password: password

                    })
                }
            );


        // =================================================
        // SAFE RESPONSE READING
        // =================================================

        const contentType =
            response.headers.get(
                "content-type"
            );

        let data = null;
        let text = "";


        if (
            contentType &&
            contentType.includes(
                "application/json"
            )
        ) {

            data =
                await response.json();

        } else {

            text =
                await response.text();
        }


        // =================================================
        // ERROR RESPONSE
        // =================================================

        if (!response.ok) {

            throw new Error(

                data?.message ||
                text ||
                "Invalid email or password"

            );
        }


        // =================================================
        // SAVE JWT TOKEN
        // =================================================

        if (data?.token) {

            localStorage.setItem(
                "token",
                data.token
            );
        }


        // =================================================
        // SAVE CUSTOMER DATA
        // =================================================

        if (data?.customerId != null) {

            localStorage.setItem(
                "customerId",
                data.customerId
            );
        }


        if (data?.email) {

            localStorage.setItem(
                "customerEmail",
                data.email
            );
        }


        if (data?.fullName) {

            localStorage.setItem(
                "customerName",
                data.fullName
            );
        }


        if (data?.role) {

            localStorage.setItem(
                "userRole",
                data.role
            );
        }


        // =================================================
        // SUCCESS
        // =================================================

        showMessage(
            "Login successful!",
            true
        );


        // =================================================
        // REDIRECT TO DASHBOARD
        // =================================================

        setTimeout(() => {

            window.location.href =
                "/dashboard.html";

        }, 700);


    } catch (error) {

        console.error(
            "Login Error:",
            error
        );


        showMessage(
            error.message ||
            "Login failed.",
            false
        );


    } finally {

        button.disabled = false;

        button.textContent =
            "Login";
    }
}


// =====================================================
// MESSAGE
// =====================================================

function showMessage(
    messageText,
    success
) {

    const element =
        document.getElementById(
            "loginMessage"
        );


    element.textContent =
        messageText;


    element.className =
        success
            ? "success"
            : "error";
}


// =====================================================
// ENTER KEY
// =====================================================

document.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Enter") {

            login();
        }

    }
);