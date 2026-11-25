document.addEventListener("DOMContentLoaded", () => {
    const list = document.getElementById("equipment-selection-list");
    const template = document.getElementById("equipment-selection-row-template");
    if (!list || !template) return;

    const functional = {
        display: document.getElementById("module-display"),
        payment: document.getElementById("module-payment"),
        printer: document.getElementById("module-printer"),
        access: [...document.querySelectorAll("[data-access-purpose]")]
    };

    const keyOf = (type, purpose) => type + ":" + purpose;

    const readSelections = () => {
        const selections = new Map();
        list.querySelectorAll(".equipment-selection-row").forEach(row => {
            const select = row.querySelector("select");
            const option = select?.selectedOptions[0];
            if (!option || !option.value || !option.dataset.functionalType || !option.dataset.purpose) return;
            const quantity = row.querySelector('input[type="number"]');
            selections.set(keyOf(option.dataset.functionalType, option.dataset.purpose), {
                id: option.value,
                quantity: quantity?.value || "1"
            });
        });
        return selections;
    };

    const activePurposes = () => {
        const purposes = new Map();
        const add = (type, purpose) => {
            if (purpose && purpose !== "NONE" && purpose !== "ABSENT") {
                purposes.set(keyOf(type, purpose), {type, purpose});
            }
        };

        add("DISPLAY", functional.display?.value);
        functional.access.filter(input => input.checked)
            .forEach(input => add("ACCESS", input.value));

        const payment = functional.payment?.value;
        if (payment === "CARDS" || payment === "CARDS_AND_CASH") add("PAYMENT", "CARDS");
        if (payment === "CASH" || payment === "CARDS_AND_CASH") add("PAYMENT", "CASH");

        add("PRINTER", functional.printer?.value);

        // Системные позиции не управляются функциональными переключателями.
        // Сохраняем их, если они уже присутствовали в конфигурации.
        readSelections().forEach((selection, key) => {
            const [type, purpose] = key.split(":");
            if (type === "SYSTEM") purposes.set(key, {type, purpose});
        });
        return purposes;
    };

    const render = () => {
        const previous = readSelections();
        const purposes = activePurposes();
        list.replaceChildren();

        purposes.forEach(({type, purpose}, key) => {
            const fragment = template.content.cloneNode(true);
            const row = fragment.querySelector(".equipment-selection-row");
            const select = row.querySelector("select");
            const quantity = row.querySelector('input[type="number"]');
            const label = row.querySelector("label");

            const placeholder = select.options[0];
            [...select.options].slice(1).forEach(option => {
                if (option.dataset.functionalType !== type || option.dataset.purpose !== purpose) {
                    option.remove();
                }
            });

            const previousSelection = previous.get(key);
            let selectedOption = previousSelection
                ? [...select.options].find(option => option.value === previousSelection.id)
                : null;
            if (!selectedOption) {
                selectedOption = [...select.options].find(option => option.dataset.default === "true");
            }
            if (selectedOption) {
                select.value = selectedOption.value;
            } else {
                select.value = "";
            }

            const index = list.children.length;
            select.name = "equipmentSelections[" + index + "].equipmentId";
            select.id = "equipment-" + index;
            quantity.name = "equipmentSelections[" + index + "].quantity";
            quantity.id = "quantity-" + index;
            quantity.value = previousSelection?.quantity || "1";
            label.htmlFor = select.id;
            label.textContent = type === "ACCESS"
                ? "Оборудование для доступа: " + purpose
                : "Оборудование: " + purpose;

            if (select.options.length === 1) {
                placeholder.textContent = "В каталоге нет моделей для этого назначения";
                select.disabled = true;
            }
            list.append(fragment);
        });
    };

    [functional.display, functional.payment, functional.printer]
        .filter(Boolean)
        .forEach(control => control.addEventListener("change", render));
    functional.access.forEach(control => control.addEventListener("change", render));

    render();
});
