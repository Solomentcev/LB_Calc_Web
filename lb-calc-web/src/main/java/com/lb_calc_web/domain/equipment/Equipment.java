package com.lb_calc_web.domain.equipment;

import java.util.Objects;

/**
 * Физическая модель оборудования, доступная для установки в модуль управления.
 *
 * <p>Идентификатор назначается хранилищем. До сохранения модели он равен
 * {@code null}; две несохранённые модели считаются разными объектами.</p>
 */
public final class Equipment {

    private final Long id;
    private final EquipmentFunctionalType functionalType;
    private final EquipmentPurpose purpose;
    private final EquipmentCategory category;
    private final String name;
    private final String description;
    private final int width;
    private final int height;
    private final int depth;

    public Equipment(
            Long id,
            EquipmentFunctionalType functionalType,
            EquipmentPurpose purpose,
            EquipmentCategory category,
            String name,
            String description,
            int width,
            int height,
            int depth
    ) {
        this.functionalType = Objects.requireNonNull(
                functionalType, "Функциональный тип оборудования не должен быть null");
        this.purpose = Objects.requireNonNull(
                purpose, "Назначение оборудования не должно быть null");
        this.category = Objects.requireNonNull(
                category, "Категория оборудования не должна быть null");

        if (!functionalType.supports(purpose)
                || category.getFunctionalType() != functionalType
                || !category.supports(purpose)) {
            throw new IllegalArgumentException(
                    "Функциональный тип, назначение и категория оборудования не согласованы");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название оборудования не должно быть пустым");
        }
        if (width < 0 || height < 0 || depth < 0) {
            throw new IllegalArgumentException("Габариты оборудования не могут быть отрицательными");
        }

        this.id = id;
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public Long getId() { return id; }
    public EquipmentFunctionalType getFunctionalType() { return functionalType; }
    public EquipmentPurpose getPurpose() { return purpose; }
    public EquipmentCategory getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Equipment other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "Equipment{id=" + id
                + ", functionalType=" + functionalType
                + ", purpose=" + purpose
                + ", category=" + category
                + ", name='" + name + "'}";
    }
}
