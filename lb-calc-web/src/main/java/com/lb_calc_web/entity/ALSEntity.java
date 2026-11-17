package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.Colors;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA-сущность автоматизированной системы хранения ALS.
 *
 * <p>ALS содержит модули в физическом порядке слева направо.
 * Положение каждого модуля хранится в {@link ALSModuleEntity},
 * поскольку один и тот же модуль может использоваться
 * в разных ALS с разным расположением.</p>
 */
@Entity
@Table(name = "als")
public class ALSEntity {

    /**
     * Идентификатор ALS.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование ALS.
     */
    @Transient
    private String name;

    /**
     * Описание ALS.
     */
    @Transient
    private String description;

    /**
     * Высота ALS, мм.
     */
    @Column(nullable = false)
    private int height;

    /**
     * Общая ширина ALS, мм.
     *
     * <p>Рассчитывается доменной моделью
     * как сумма ширин модулей.</p>
     */
    @Transient
    private int width;

    /**
     * Глубина ALS, мм.
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
     * Модули, входящие в ALS.
     *
     * <p>Список отсортирован по физическому расположению
     * модулей слева направо.</p>
     */
    @OneToMany(
            mappedBy = "als",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("moduleOrder ASC")
    private List<ALSModuleEntity> modules = new ArrayList<>();

    /**
     * Конструктор для JPA.
     */
    public ALSEntity() {
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
                "Название ALS не должно быть null"
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

    public List<ALSModuleEntity> getModules() {
        return modules;
    }

    /**
     * Добавляет модуль в указанную физическую позицию ALS.
     *
     * @param module модуль
     * @param moduleOrder позиция слева направо, начиная с нуля
     */
    public void addModule(ModuleEntity module, int moduleOrder) {
        ALSModuleEntity association = new ALSModuleEntity();
        association.setAls(this);
        association.setModule(Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        ));
        association.setModuleOrder(moduleOrder);
        modules.add(association);
    }

    /**
     * Удаляет все связи ALS с модулями.
     *
     * <p>Коллекция остаётся тем же JPA-managed экземпляром.</p>
     */
    public void clearModules() {
        modules.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ALSEntity that = (ALSEntity) o;

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
        return "ALSEntity{" +
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
                ", modules=" + modules +
                '}';
    }
}