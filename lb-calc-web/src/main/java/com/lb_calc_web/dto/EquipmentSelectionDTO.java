package com.lb_calc_web.dto;

import java.util.Objects;

/** Выбранная физическая модель оборудования и её количество. */
public class EquipmentSelectionDTO {

    private Long equipmentId;
    private int quantity = 1;

    public EquipmentSelectionDTO() {
    }

    public EquipmentSelectionDTO(Long equipmentId, int quantity) {
        setEquipmentId(equipmentId);
        setQuantity(quantity);
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        if (equipmentId != null && equipmentId <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор модели оборудования должен быть положительным");
        }
        this.equipmentId = equipmentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Количество оборудования должно быть не меньше 1");
        }
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EquipmentSelectionDTO other)) return false;
        return quantity == other.quantity && Objects.equals(equipmentId, other.equipmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentId, quantity);
    }
}
