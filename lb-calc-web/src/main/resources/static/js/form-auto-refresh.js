document.addEventListener("pointerdown", event => {
    const submitter = event.target.closest("button[type=submit], input[type=submit]");
    if (submitter?.form && !submitter.matches("[data-form-refresh]")) {
        submitter.form.dataset.skipAutoRefresh = "true";
    }
}, true);

document.addEventListener("click", event => {
    const submitter = event.target.closest("button[type=submit], input[type=submit]");
    if (submitter?.form) {
        delete submitter.form.dataset.skipAutoRefresh;
    }
}, true);

document.addEventListener("change", event => {
    const field = event.target;
    if (!(field instanceof HTMLElement)
            || !field.matches("input:not([type=hidden]):not([readonly]):not([disabled]), select:not([disabled]), textarea:not([readonly]):not([disabled])")) {
        return;
    }

    const form = field.closest("form");
    if (form?.dataset.skipAutoRefresh) {
        delete form.dataset.skipAutoRefresh;
        return;
    }

    const refreshButton = form?.querySelector("[data-form-refresh]");
    if (refreshButton) {
        form.requestSubmit(refreshButton);
    }
});
