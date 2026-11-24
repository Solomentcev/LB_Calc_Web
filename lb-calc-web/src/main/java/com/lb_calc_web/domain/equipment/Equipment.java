package com.lb_calc_web.domain.equipment;

import java.util.Objects;

/** Конкретная позиция оборудования из каталога. */
public class Equipment {
    private final String catalogPositionId;
    private final EquipmentFunctionalType functionalType;
    private final EquipmentPurpose purpose;
    private final EquipmentCategory equipmentCategory;
    private final String name;
    private final String description;
    private final int width;
    private final int height;
    private final int depth;

    /**
     * Создаёт каталожную позицию с явно заданной функциональной принадлежностью.
     */
    public Equipment(String catalogPositionId,
                     EquipmentFunctionalType functionalType,
                     EquipmentPurpose purpose,
                     EquipmentCategory equipmentCategory,
                     String name,
                     String description,
                     int width,
                     int height,
                     int depth) {
        if (catalogPositionId == null || catalogPositionId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор позиции каталога не должен быть пустым");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название оборудования не должно быть пустым");
        }
        this.functionalType = Objects.requireNonNull(functionalType,
                "Функциональный тип оборудования не должен быть null");
        this.purpose = Objects.requireNonNull(purpose,
                "Назначение оборудования не должно быть null");
        this.equipmentCategory = Objects.requireNonNull(equipmentCategory,
                "Категория оборудования не должна быть null");
        if (!functionalType.supports(purpose)
                || equipmentCategory.getFunctionalType() != functionalType
                || !equipmentCategory.supports(purpose)) {
            throw new IllegalArgumentException(
                    "Функциональный тип, назначение и категория оборудования не согласованы");
        }
        if (width < 0 || height < 0 || depth < 0) {
            throw new IllegalArgumentException("Габариты оборудования не могут быть отрицательными");
        }
        this.catalogPositionId = catalogPositionId;
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public String getCatalogPositionId() { return catalogPositionId; }
    public EquipmentFunctionalType getFunctionalType() { return functionalType; }
    public EquipmentPurpose getPurpose() { return purpose; }
    public EquipmentCategory getEquipmentCategory() { return equipmentCategory; }
    public String getCategory() { return equipmentCategory.name(); }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }

    public boolean isDisplay() { return equipmentCategory == EquipmentCategory.DISPLAY; }
    public boolean isBarReader() { return equipmentCategory == EquipmentCategory.BAR_READER; }
    public boolean isPrinter() { return equipmentCategory == EquipmentCategory.PRINTER; }
    public boolean isRfidReader() { return equipmentCategory == EquipmentCategory.RFID_READER; }

    @Override
    public final boolean equals(Object object) {
        return this == object || object instanceof Equipment other
                && catalogPositionId.equals(other.catalogPositionId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(catalogPositionId);
    }

    @Override
    public String toString() {
        return "Equipment{catalogPositionId='" + catalogPositionId
                + "', functionalType=" + functionalType
                + ", purpose=" + purpose
                + ", category=" + equipmentCategory
                + ", name='" + name + "'}";
    }
}
