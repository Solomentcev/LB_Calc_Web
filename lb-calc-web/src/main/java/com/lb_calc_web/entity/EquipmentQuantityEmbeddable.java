package com.lb_calc_web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Сохранённая позиция оборудования и её количество.
 */
@Embeddable
public class EquipmentQuantityEmbeddable {

    @Column(name = "catalog_position_id", nullable = false)
    private String catalogPositionId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /**
     * Конструктор для JPA.
     */
    protected EquipmentQuantityEmbeddable() {
    }

    public EquipmentQuantityEmbeddable(String catalogPositionId, int quantity) {
        if (catalogPositionId == null || catalogPositionId.isBlank()) {
            throw new IllegalArgumentException("ID позиции оборудования не должен быть пустым");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
        }
        this.catalogPositionId = catalogPositionId;
        this.quantity = quantity;
    }

    public String getCatalogPositionId() {
        return catalogPositionId;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EquipmentQuantityEmbeddable other)) return false;
        return quantity == other.quantity
                && Objects.equals(catalogPositionId, other.catalogPositionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalogPositionId, quantity);
    }
}
