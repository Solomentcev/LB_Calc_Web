package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.dto.EquipmentSelectionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Объединяет сохранённый состав оборудования с изменением выбора дисплея в форме. */
public final class EquipmentSelectionMapper {

    private EquipmentSelectionMapper() {
    }

    /**
     * Сохраняет выбранные модели всех категорий, кроме дисплея, и применяет
     * новое значение дисплея из legacy-формы.
     *
     * @param existingSelections сохранённый состав оборудования
     * @param displayName выбранное в форме имя или ID дисплея
     * @param displayQuantity количество дисплеев; null означает одну единицу
     * @param equipmentResolver разрешение каталожной позиции
     * @return объединённый состав оборудования
     */
    public static List<EquipmentSelectionDTO> mergeDisplaySelection(
            List<EquipmentSelectionDTO> existingSelections,
            String displayName,
            Integer displayQuantity,
            Function<String, Equipment> equipmentResolver
    ) {
        Objects.requireNonNull(existingSelections, "Сохранённый состав оборудования не должен быть null");
        Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");

        List<EquipmentSelectionDTO> result = new ArrayList<>();
        EquipmentSelectionDTO existingDisplaySelection = null;
        Equipment existingDisplay = null;

        for (EquipmentSelectionDTO selection : existingSelections) {
            Equipment equipment = Objects.requireNonNull(
                    equipmentResolver.apply(selection.catalogPositionId()),
                    "Позиция оборудования не найдена: " + selection.catalogPositionId()
            );
            if (equipment.isDisplay()) {
                if (existingDisplaySelection != null) {
                    throw new IllegalArgumentException(
                            "В сохранённой конфигурации найдено несколько дисплеев");
                }
                existingDisplaySelection = selection;
                existingDisplay = equipment;
            } else {
                result.add(selection);
            }
        }

        if (displayQuantity != null && displayQuantity < 0) {
            throw new IllegalArgumentException("Количество дисплеев не может быть отрицательным");
        }

        if (displayName == null || displayName.isBlank()) {
            if (existingDisplaySelection != null) {
                result.add(existingDisplaySelection);
            }
            return List.copyOf(result);
        }

        if (Display.NONE_ID.equals(displayName)
                || Display.NONE.getName().equals(displayName)
                || Integer.valueOf(0).equals(displayQuantity)) {
            return List.copyOf(result);
        }

        if (existingDisplay != null
                && (displayName.equals(existingDisplay.getName())
                || displayName.equals(existingDisplay.getCatalogPositionId()))) {
            result.add(existingDisplaySelection);
            return List.copyOf(result);
        }

        Equipment selectedDisplay = Objects.requireNonNull(
                equipmentResolver.apply(displayName),
                "Выбранный дисплей не найден: " + displayName
        );
        if (!selectedDisplay.isDisplay()) {
            throw new IllegalArgumentException(
                    "Выбранная позиция не относится к категории DISPLAY: " + displayName);
        }

        result.add(new EquipmentSelectionDTO(
                selectedDisplay.getCatalogPositionId(),
                displayQuantity == null ? 1 : displayQuantity
        ));
        return List.copyOf(result);
    }
}
