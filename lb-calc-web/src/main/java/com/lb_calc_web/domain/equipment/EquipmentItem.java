package com.lb_calc_web.domain.equipment;

import java.util.Objects;

/** Позиция оборудования в конфигурации с отдельным количеством. */
public record EquipmentItem(Equipment equipment, int quantity) {
    public EquipmentItem {
        Objects.requireNonNull(equipment, "Позиция оборудования не должна быть null");
        if (quantity < 1) {
            throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
        }
        if (equipment.isDisplay() && quantity != 1) {
            throw new IllegalArgumentException("Количество дисплеев в модуле должно быть равно 1");
        }
    }
}