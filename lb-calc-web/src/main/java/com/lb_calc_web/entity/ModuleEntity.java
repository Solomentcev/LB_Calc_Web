package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.Colors;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Базовая JPA-сущность модуля ALS.
 *
 * <p>Общие свойства модулей хранятся в таблице {@code module}.
 * Конкретные типы модулей имеют собственные таблицы:
 * {@link LCEntity}, {@link LBEntity} и {@link LBCEntity}.</p>
 */
@Entity
@Table(name = "module")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ModuleEntity {

    /**
     * Идентификатор модуля.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование модуля.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Описание модуля.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Высота модуля, мм.
     */
    @Column(nullable = false)
    private int height;

    /**
     * Ширина модуля, мм.
     */
    @Column(nullable = false)
    private int width;

    /**
     * Глубина модуля, мм.
     */
    @Column(nullable = false)
    private int depth;

    /**
     * Размер верхней рамы, мм.
     */
    @Column(nullable = false)
    private int upperFrame;

    /**
     * Размер нижней рамы, мм.
     */
    @Column(nullable = false)
    private int bottomFrame;

    /**
     * Цвет корпуса.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Colors colorBody;

    /**
     * Цвет дверей.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Colors colorDoor;

    /**
     * Конструктор для JPA.
     */
    protected ModuleEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(
                name,
                "Наименование модуля не должно быть null"
        );
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getUpperFrame() {
        return upperFrame;
    }

    public void setUpperFrame(int upperFrame) {
        this.upperFrame = upperFrame;
    }

    public int getBottomFrame() {
        return bottomFrame;
    }

    public void setBottomFrame(int bottomFrame) {
        this.bottomFrame = bottomFrame;
    }

    public Colors getColorBody() {
        return colorBody;
    }

    public void setColorBody(Colors colorBody) {
        this.colorBody = colorBody;
    }

    public Colors getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(Colors colorDoor) {
        this.colorDoor = colorDoor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleEntity that = (ModuleEntity) o;

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
        return "ModuleEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", colorBody=" + colorBody +
                ", colorDoor=" + colorDoor +
                '}';
    }
}