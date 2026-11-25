document.addEventListener("change", event => {
    const field = event.target;
    if (!(field instanceof HTMLElement)
            || !field.matches("input:not([type=hidden]):not([readonly]):not([disabled]), select:not([disabled]), textarea:not([readonly]):not([disabled])")) {
        return;
    }

    const form = field.closest("form");
    const refreshButton = form?.querySelector("[data-form-refresh]");
    if (refreshButton) {
        form.requestSubmit(refreshButton);
    }
});
