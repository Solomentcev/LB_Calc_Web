package com.lb_calc_web.controller;

import com.lb_calc_web.dto.EquipmentDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LCDTO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Преобразует строки выбора оборудования из формы в контракт комплектации DTO. */
final class ModuleEquipmentFormSupport {

    private ModuleEquipmentFormSupport() {
    }

    static void applySelection(LCDTO dto) {
        Objects.requireNonNull(dto, "LC не должен быть null");
        dto.setEquipment(toEquipmentMap(dto.getSelectedEquipment()));
        dto.setEquipmentProvided(true);
    }

    static void applySelection(LBCDTO dto) {
        Objects.requireNonNull(dto, "LBC не должен быть null");
        dto.setEquipment(toEquipmentMap(dto.getSelectedEquipment()));
        dto.setEquipmentProvided(true);
    }

    private static Map<Long, Integer> toEquipmentMap(Iterable<EquipmentDTO> selections) {
        Map<Long, Integer> equipment = new LinkedHashMap<>();
        for (EquipmentDTO selection : selections) {
            if (selection == null || selection.getId() == null || selection.getId() <= 0) {
                throw new IllegalArgumentException("Выберите модель оборудования");
            }
            Integer quantity = selection.getQuantity();
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
            }
            if (equipment.putIfAbsent(selection.getId(), quantity) != null) {
                throw new IllegalArgumentException("Одна модель оборудования не может быть указана несколько раз");
            }
        }
        return equipment;
    }
}
