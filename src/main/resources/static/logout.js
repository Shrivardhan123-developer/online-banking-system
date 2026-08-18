// =====================================================
// LOGOUT
// =====================================================

function logout() {

    localStorage.removeItem(
        "customerId"
    );

    localStorage.removeItem(
        "customerEmail"
    );

    localStorage.removeItem(
        "customerName"
    );

    localStorage.removeItem(
        "userRole"
    );

    localStorage.removeItem(
        "token"
    );

    window.location.href =
        "/login.html";
}


// =====================================================
// CHECK LOGIN
// =====================================================

function isLoggedIn() {

    return !!localStorage.getItem(
        "customerId"
    );
}