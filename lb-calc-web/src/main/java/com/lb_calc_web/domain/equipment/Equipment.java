package com.lb_calc_web.domain.equipment;

import java.util.Objects;

/** Базовая модель позиции оборудования из каталога. */
public abstract class Equipment {
    private final String catalogPositionId;
    private final String name;

    protected Equipment(String catalogPositionId, String name) {
        if (catalogPositionId == null || catalogPositionId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор позиции каталога не должен быть пустым");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название оборудования не должно быть пустым");
        }
        this.catalogPositionId = catalogPositionId;
        this.name = name;
    }

    public String getCatalogPositionId() { return catalogPositionId; }
    public String getName() { return name; }
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract int getDepth();
    public abstract String getDescription();

    @Override
    public final boolean equals(Object o) {
        return this == o || o instanceof Equipment that
                && catalogPositionId.equals(that.catalogPositionId);
    }

    @Override
    public final int hashCode() { return Objects.hash(catalogPositionId); }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{catalogPositionId='" +
                catalogPositionId + "', name='" + name + "'}";
    }
}