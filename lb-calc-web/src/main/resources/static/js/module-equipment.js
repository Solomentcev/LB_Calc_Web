document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("module-display")?.form;
    const refreshButton = form?.querySelector("button[formaction]");
    if (!form || !refreshButton) return;

    form.querySelectorAll(
        "#module-display, #module-payment, #module-printer, [data-access-purpose]"
    ).forEach(control => {
        control.addEventListener("change", () => form.requestSubmit(refreshButton));
    });
});
