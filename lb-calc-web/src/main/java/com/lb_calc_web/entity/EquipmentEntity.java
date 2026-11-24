package com.lb_calc_web.entity;

import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * JPA-сущность физической модели оборудования.
 *
 * <p>Назначение хранится строкой, поскольку конкретный enum определяется
 * функциональным типом оборудования.</p>
 */
@Entity
@Table(name = "equipment")
public class EquipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "functional_type", nullable = false, length = 50)
    private EquipmentFunctionalType functionalType;

    @Column(nullable = false, length = 50)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EquipmentCategory category;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private int depth;

    protected EquipmentEntity() {
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EquipmentFunctionalType getFunctionalType() { return functionalType; }
    public String getPurpose() { return purpose; }
    public EquipmentCategory getCategory() { return category; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название оборудования не должно быть пустым");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    public void setFunctionalType(EquipmentFunctionalType functionalType) {
        this.functionalType = Objects.requireNonNull(functionalType,
                "Функциональный тип оборудования не должен быть null");
    }

    public void setPurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Назначение оборудования не должно быть пустым");
        }
        this.purpose = purpose;
    }

    public void setCategory(EquipmentCategory category) {
        this.category = Objects.requireNonNull(category,
                "Категория оборудования не должна быть null");
    }

    public void setWidth(int width) {
        this.width = requireNonNegative(width, "Ширина");
    }

    public void setHeight(int height) {
        this.height = requireNonNegative(height, "Высота");
    }

    public void setDepth(int depth) {
        this.depth = requireNonNegative(depth, "Глубина");
    }

    private int requireNonNegative(int value, String dimension) {
        if (value < 0) {
            throw new IllegalArgumentException(dimension + " оборудования не может быть отрицательной");
        }
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EquipmentEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
