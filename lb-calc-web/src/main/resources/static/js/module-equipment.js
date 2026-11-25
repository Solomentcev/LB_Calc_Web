document.addEventListener("DOMContentLoaded", () => {
    const list = document.getElementById("equipment-selection-list");
    const template = document.getElementById("equipment-selection-row-template");
    const addButton = document.getElementById("add-equipment");

    if (!list || !template || !addButton) {
        return;
    }

    const reindex = () => {
        list.querySelectorAll(".equipment-selection-row").forEach((row, index) => {
            row.querySelectorAll("[name]").forEach((field) => {
                field.name = field.name.replace(
                    /equipmentSelections\[\d+]/,
                    "equipmentSelections[" + index + "]"
                );
            });

            const select = row.querySelector("select");
            const quantity = row.querySelector('input[type="number"]');
            const label = row.querySelectorAll("label");

            if (select) {
                select.id = "equipment-" + index;
            }
            if (quantity) {
                quantity.id = "quantity-" + index;
            }
            if (label[0] && select) {
                label[0].htmlFor = select.id;
            }
            if (label[1] && quantity) {
                label[1].htmlFor = quantity.id;
            }
        });
    };

    addButton.addEventListener("click", () => {
        const index = list.querySelectorAll(".equipment-selection-row").length;
        const fragment = template.content.cloneNode(true);

        fragment.querySelectorAll("[name]").forEach((field) => {
            field.name = field.name.replace(/__INDEX__/g, String(index));
        });

        list.append(fragment);
        reindex();
    });

    list.addEventListener("click", (event) => {
        const removeButton = event.target.closest(".remove-equipment");
        if (!removeButton) {
            return;
        }

        const row = removeButton.closest(".equipment-selection-row");
        if (row) {
            row.remove();
            reindex();
        }
    });

    reindex();
});
