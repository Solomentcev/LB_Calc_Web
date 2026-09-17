package com.lb_calc_web.entity;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Связь между ALS и модулем.
 *
 * <p>Хранит положение конкретного модуля внутри конкретной ALS.</p>
 *
 * <p>Один и тот же модуль может входить в несколько ALS
 * и иметь в каждой из них своё положение.</p>
 */
@Entity
@Table(
        name = "als_module",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_als_module_order",
                        columnNames = {"als_id", "module_order"}
                )
        }
)
public class ALSModuleEntity {

    /**
     * Идентификатор связи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ALS, в которую входит модуль.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "als_id",
            nullable = false
    )
    private ALSEntity als;

    /**
     * Модуль.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "module_id",
            nullable = false
    )
    private ModuleEntity module;

    /**
     * Порядковый номер модуля внутри ALS.
     *
     * <p>Нумерация начинается с нуля и соответствует
     * физическому расположению слева направо.</p>
     */
    @Column(name = "module_order", nullable = false)
    private int moduleOrder;

    /**
     * Конструктор для JPA.
     */
    protected ALSModuleEntity() {
    }

    public Long getId() {
        return id;
    }

    public ALSEntity getAls() {
        return als;
    }

    public void setAls(ALSEntity als) {
        this.als = Objects.requireNonNull(
                als,
                "ALS не должна быть null"
        );
    }

    public ModuleEntity getModule() {
        return module;
    }

    public void setModule(ModuleEntity module) {
        this.module = Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        );
    }

    public int getModuleOrder() {
        return moduleOrder;
    }

    public void setModuleOrder(int moduleOrder) {
        if (moduleOrder < 0) {
            throw new IllegalArgumentException(
                    "Порядковый номер модуля не может быть отрицательным"
            );
        }

        this.moduleOrder = moduleOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ALSModuleEntity that = (ALSModuleEntity) o;

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ALSModuleEntity{" +
                "id=" + id +
                ", alsId=" + (
                als == null ? null : als.getId()
        ) +
                ", moduleId=" + (
                module == null ? null : module.getId()
        ) +
                ", moduleOrder=" + moduleOrder +
                '}';
    }
}