package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

/**
 * Базовая JPA-модель для модулей хранения.
 *
 * <p>Класс не создаёт отдельную таблицу. Его поля наследуются
 * конкретными сущностями {@link LBEntity} и {@link LBCEntity}.</p>
 *
 * <p>Модель хранения в Entity отличается от domain-модели:
 * здесь фиксируются значения, необходимые для сохранения состояния
 * в базе данных. Объект {@code TypeLb} из domain хранится как набор
 * простых полей.</p>
 */
@MappedSuperclass
public abstract class StorageModuleEntity extends ModuleEntity {

    /**
     * Название конструктивного типа модуля.
     *
     * <p>Соответствует {@code TypeLb.type} в domain.</p>
     */
    @Column(name = "type", nullable = false)
    private String type;

    /**
     * Добавочная ширина конструкции модуля, мм.
     *
     * <p>Соответствует {@code TypeLb.deltaWidth} в domain.</p>
     */
    @Column(name = "delta_width", nullable = false)
    private int deltaWidth;

    /**
     * Толщина полки между ячейками, мм.
     *
     * <p>Соответствует {@code TypeLb.shelfThick} в domain.</p>
     */
    @Column(name = "shelf_thick", nullable = false)
    private int shelfThick;

    /**
     * Ширина сервисной зоны, мм.
     *
     * <p>Соответствует {@code TypeLb.serviceZoneWidth} в domain.</p>
     */
    @Column(name = "service_zone_width", nullable = false)
    private int serviceZoneWidth;

    /**
     * Толщина дверцы, мм.
     */
    @Column(name = "door_thickness", nullable = false)
    private int doorThickness;

    /**
     * Направление открытия дверцы.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "direction_door_opening", nullable = false)
    private DirectionDoorOpening directionDoorOpening;

    /**
     * Количество ячеек модуля.
     */
    @Column(name = "count_cells", nullable = false)
    private int countCells;

    /**
     * Рассчитанная высота ячейки, мм.
     */
    @Column(name = "height_cell", nullable = false)
    private int heightCell;

    /**
     * Рассчитанная ширина ячейки, мм.
     */
    @Column(name = "width_cell", nullable = false)
    private int widthCell;

    /**
     * Рассчитанная глубина ячейки, мм.
     */
    @Column(name = "depth_cell", nullable = false)
    private int depthCell;

    /**
     * Конструктор для JPA.
     */
    protected StorageModuleEntity() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDeltaWidth() {
        return deltaWidth;
    }

    public void setDeltaWidth(int deltaWidth) {
        this.deltaWidth = deltaWidth;
    }

    public int getShelfThick() {
        return shelfThick;
    }

    public void setShelfThick(int shelfThick) {
        this.shelfThick = shelfThick;
    }

    public int getServiceZoneWidth() {
        return serviceZoneWidth;
    }

    public void setServiceZoneWidth(int serviceZoneWidth) {
        this.serviceZoneWidth = serviceZoneWidth;
    }

    public int getDoorThickness() {
        return doorThickness;
    }

    public void setDoorThickness(int doorThickness) {
        this.doorThickness = doorThickness;
    }

    public DirectionDoorOpening getDirectionDoorOpening() {
        return directionDoorOpening;
    }

    public void setDirectionDoorOpening(
            DirectionDoorOpening directionDoorOpening
    ) {
        this.directionDoorOpening = directionDoorOpening;
    }

    public int getCountCells() {
        return countCells;
    }

    public void setCountCells(int countCells) {
        this.countCells = countCells;
    }

    public int getHeightCell() {
        return heightCell;
    }

    public void setHeightCell(int heightCell) {
        this.heightCell = heightCell;
    }

    public int getWidthCell() {
        return widthCell;
    }

    public void setWidthCell(int widthCell) {
        this.widthCell = widthCell;
    }

    public int getDepthCell() {
        return depthCell;
    }

    public void setDepthCell(int depthCell) {
        this.depthCell = depthCell;
    }

    @Override
    public String toString() {
        return "StorageModuleEntity{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", type='" + type + '\'' +
                ", deltaWidth=" + deltaWidth +
                ", shelfThick=" + shelfThick +
                ", serviceZoneWidth=" + serviceZoneWidth +
                ", doorThickness=" + doorThickness +
                ", directionDoorOpening=" + directionDoorOpening +
                ", countCells=" + countCells +
                ", heightCell=" + heightCell +
                ", widthCell=" + widthCell +
                ", depthCell=" + depthCell +
                '}';
    }
}