package com.lb_calc_web.entity;

import jakarta.persistence.*;

import java.util.Objects;

/** Позиция установленной физической модели оборудования и её количество. */
@Embeddable
public class EquipmentQuantityEmbeddable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    @Column(nullable = false)
    private int quantity;

    protected EquipmentQuantityEmbeddable() {
    }

    public EquipmentQuantityEmbeddable(EquipmentEntity equipment, int quantity) {
        this.equipment = Objects.requireNonNull(
                equipment, "Модель оборудования не должна быть null");
        setQuantity(quantity);
    }

    public EquipmentEntity getEquipment() {
        return equipment;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
        }
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EquipmentQuantityEmbeddable other)) return false;
        return quantity == other.quantity
                && Objects.equals(equipment, other.equipment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipment, quantity);
    }
}
