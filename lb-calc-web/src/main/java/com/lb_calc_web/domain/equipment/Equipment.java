package com.lb_calc_web.domain.equipment;

import java.util.Objects;

/** Конкретная модель физического оборудования. */
public class Equipment {
    private final String id;
    private final EquipmentFunctionalType functionalType;
    private final EquipmentPurpose purpose;
    private final EquipmentCategory equipmentCategory;
    private final String name;
    private final String description;
    private final int width;
    private final int height;
    private final int depth;

    /**
     * Создаёт модель оборудования.
     *
     * @param id идентификатор модели
     * @param functionalType функциональная область
     * @param purpose назначение модели
     * @param equipmentCategory физическая категория
     * @param name наименование
     * @param description описание
     * @param width ширина, мм
     * @param height высота, мм
     * @param depth глубина, мм
     */
    public Equipment(String id,
                     EquipmentFunctionalType functionalType,
                     EquipmentPurpose purpose,
                     EquipmentCategory equipmentCategory,
                     String name,
                     String description,
                     int width,
                     int height,
                     int depth) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Идентификатор оборудования не должен быть пустым");
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
        this.id = id;
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public String getId() { return id; }
    public EquipmentFunctionalType getFunctionalType() { return functionalType; }
    public EquipmentPurpose getPurpose() { return purpose; }
    public EquipmentCategory getEquipmentCategory() { return equipmentCategory; }
    public String getCategory() { return equipmentCategory.name(); }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }

    /**
     * Возвращает идентификатор модели в прежнем формате.
     *
     * @return идентификатор оборудования
     * @deprecated используйте {@link #getId()}
     */
    @Deprecated
    public String getCatalogPositionId() { return id; }

    public boolean isDisplay() { return equipmentCategory == EquipmentCategory.DISPLAY; }
    public boolean isBarReader() { return equipmentCategory == EquipmentCategory.BAR_READER; }
    public boolean isPrinter() { return equipmentCategory == EquipmentCategory.PRINTER; }
    public boolean isRfidReader() { return equipmentCategory == EquipmentCategory.RFID_READER; }

    @Override
    public final boolean equals(Object object) {
        return this == object || object instanceof Equipment other
                && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Equipment{id='" + id
                + "', functionalType=" + functionalType
                + ", purpose=" + purpose
                + ", category=" + equipmentCategory
                + ", name='" + name + "'}";
    }
}
