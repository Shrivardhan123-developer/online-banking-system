// =====================================================
// BANKFLOW APP
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        const customerName =
            localStorage.getItem(
                "customerName"
            );


        const elements =
            document.querySelectorAll(
                "[data-customer-name]"
            );


        elements.forEach(element => {

            element.textContent =
                customerName || "Customer";

        });

    }
);