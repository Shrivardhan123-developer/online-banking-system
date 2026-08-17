const API = "/api";

// =====================================================
// REGISTER
// =====================================================

async function registerCustomer() {

    const fullName =
        document.getElementById("fullName").value.trim();

    const email =
        document.getElementById("email").value.trim();

    const phone =
        document.getElementById("phone").value.trim();

    const password =
        document.getElementById("password").value;

    const button =
        document.getElementById("registerButton");

    // =====================================================
    // VALIDATION
    // =====================================================

    if (!fullName) {
        showMessage(
            "Full name is required.",
            false
        );
        return;
    }

    if (!email) {
        showMessage(
            "Email is required.",
            false
        );
        return;
    }

    if (!phone) {
        showMessage(
            "Phone number is required.",
            false
        );
        return;
    }

    if (!password) {
        showMessage(
            "Password is required.",
            false
        );
        return;
    }

    if (password.length < 6) {
        showMessage(
            "Password must contain at least 6 characters.",
            false
        );
        return;
    }

    // =====================================================
    // SEND REQUEST
    // =====================================================

    try {

        button.disabled = true;
        button.textContent = "Creating Account...";

        const response =
            await fetch(
                `${API}/auth/register`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        fullName: fullName,
                        email: email,
                        phone: phone,
                        password: password
                    })
                }
            );

        // =================================================
        // READ RESPONSE SAFELY
        // =================================================

        const contentType =
            response.headers.get("content-type");

        let data = null;
        let text = "";

        if (contentType &&
            contentType.includes("application/json")) {

            data = await response.json();

        } else {

            text = await response.text();
        }

        // =================================================
        // HANDLE ERROR
        // =================================================

        if (!response.ok) {

            throw new Error(
                data?.message ||
                text ||
                `Registration failed. Status: ${response.status}`
            );
        }

        // =================================================
        // SUCCESS
        // =================================================

        showMessage(
            "Account created successfully!",
            true
        );

        setTimeout(() => {

            window.location.href =
                "/login.html";

        }, 1000);

    } catch (error) {

        console.error(
            "Registration Error:",
            error
        );

        showMessage(
            error.message ||
            "Registration failed.",
            false
        );

    } finally {

        button.disabled = false;

        button.textContent =
            "Create Account";
    }
}

// =====================================================
// MESSAGE
// =====================================================

function showMessage(
    text,
    success
) {

    const element =
        document.getElementById(
            "registerMessage"
        );

    element.textContent =
        text;

    element.className =
        success
            ? "success"
            : "error";
}