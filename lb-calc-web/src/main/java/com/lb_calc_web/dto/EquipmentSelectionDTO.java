package com.lb_calc_web.dto;

/** Выбор позиции оборудования из каталога и её количество. */
public record EquipmentSelectionDTO(String catalogPositionId, int quantity) {
    public EquipmentSelectionDTO {
        if (catalogPositionId == null || catalogPositionId.isBlank()) {
            throw new IllegalArgumentException("ID позиции оборудования не должен быть пустым");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
        }
    }
}
