package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;

import java.util.Map;
import java.util.Objects;

/**
 * Комбинированный модуль LBC.
 *
 * <p>LBC одновременно является {@link StorageModule} и
 * {@link ControlModule}. Он содержит ячейки хранения и выполняет
 * функции управления.</p>
 *
 * <p>Количество ячеек определяется фактическим количеством объектов
 * {@link StorageCell}. Номера ячеек назначаются агрегатом {@link ALS}
 * в зависимости от положения LBC в конкретном ALS.</p>
 *
 * <p>Размеры ячеек, наименование и описание являются производными
 * значениями и пересчитываются методом {@link #recalculate()}.</p>
 */
public class LBC extends StorageModule implements ControlModule {

    /** Конфигурация функций управления LBC. */
    private ControlConfiguration controlConfiguration;

    /**
     * Создаёт LBC с заданной конфигурацией.
     *
     * @param height высота модуля, мм
     * @param width ширина модуля, мм
     * @param depth глубина модуля, мм
     * @param upperFrame размер верхней рамы, мм
     * @param bottomFrame размер нижней рамы, мм
     * @param colorBody цвет корпуса
     * @param colorDoor цвет дверей
     * @param typeLb конструктивный тип LB
     * @param countCells количество ячеек
     * @param directionDoorOpening направление открытия дверей
     * @param doorThickness толщина дверцы, мм
     * @param controlConfiguration конфигурация управления
     */
    public LBC(int height, int width, int depth, int upperFrame, int bottomFrame,
               Colors colorBody, Colors colorDoor, TypeLb typeLb, int countCells,
               DirectionDoorOpening directionDoorOpening, int doorThickness,
               ControlConfiguration controlConfiguration) {
        super(doorThickness);
        setHeight(height);
        setWidth(width);
        setDepth(depth);
        setUpperFrame(upperFrame);
        setBottomFrame(bottomFrame);
        setColorBody(colorBody);
        setColorDoor(colorDoor);
        setTypeLb(typeLb);
        setDirectionDoorOpening(directionDoorOpening);
        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration,
                "Конфигурация управления не должна быть null"
        ).copy();
        setCells(createCells(countCells));
        recalculate();
    }

    /** @return независимая копия конфигурации управления LBC */
    @Override
    public ControlConfiguration getControlConfiguration() {
        return controlConfiguration.copy();
    }

    /** Заменяет функциональные назначения, сохраняя установленное оборудование. */
    @Override
    public void setFunctionalConfiguration(FunctionalConfiguration configuration) {
        controlConfiguration.setFunctionalConfiguration(configuration);
        recalculate();
    }

    /**
     * Заменяет конфигурацию управления LBC.
     *
     * @param controlConfiguration новая конфигурация управления
     */
    @Override
    public void setControlConfiguration(ControlConfiguration controlConfiguration) {
        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration,
                "Конфигурация управления не должна быть null"
        ).copy();
        recalculate();
    }

    /** @return позиции оборудования LBC */
    @Override
    public Map<Equipment, Integer> getEquipment() {
        return controlConfiguration.getEquipment();
    }

    /**
     * Добавляет позицию оборудования в конфигурацию управления.
     *
     * @param equipment добавляемое оборудование
     */
    @Override
    public void addEquipment(Equipment equipment, int quantity) {
        controlConfiguration.addEquipment(
                Objects.requireNonNull(equipment, "Оборудование не должно быть null"), quantity);
        recalculate();
    }

    /**
     * Удаляет позицию оборудования из конфигурации управления.
     *
     * @param equipment удаляемое оборудование
     */
    @Override
    public void removeEquipment(Equipment equipment) {
        controlConfiguration.removeEquipment(
                Objects.requireNonNull(equipment, "Оборудование не должно быть null"));
        recalculate();
    }

    /** @return максимальная высота установленного оборудования, мм */
    @Override
    public int getEquipmentHeight() {
        return getEquipment().keySet().stream()
                .mapToInt(Equipment::getHeight)
                .max()
                .orElse(0);
    }

    /** Пересчитывает размеры ячеек LBC. */
    @Override
    public void recalculateCells() {
        recalculateStorageCells();
    }

    /** Пересчитывает производные характеристики LBC. */
    @Override
    public void recalculate() {
        recalculateCells();
        setName(ModuleTextFormatter.combinedName(this, this));
        setDescription(ModuleTextFormatter.storageDescription(
                this, "Модуль хранения и управления")
                + "\n\n" + ModuleTextFormatter.controlDetailsDescription(this));
    }

    /** @return независимая копия LBC */
    @Override
    public LBC copy() {
        LBC copy = new LBC(
                getHeight(), getWidth(), getDepth(),
                getUpperFrame(), getBottomFrame(),
                getColorBody(), getColorDoor(), getTypeLb(),
                getCountCells(), getDirectionDoorOpening(),
                getDoorThickness(), controlConfiguration);
        copy.copyCellNumbersFrom(this);
        return copyTextTo(copyIdentityTo(copy));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LBC other = (LBC) object;
        return getHeight() == other.getHeight()
                && getWidth() == other.getWidth()
                && getDepth() == other.getDepth()
                && getUpperFrame() == other.getUpperFrame()
                && getBottomFrame() == other.getBottomFrame()
                && getDoorThickness() == other.getDoorThickness()
                && getCountCells() == other.getCountCells()
                && Objects.equals(getTypeLb(), other.getTypeLb())
                && getDirectionDoorOpening() == other.getDirectionDoorOpening()
                && getColorBody() == other.getColorBody()
                && getColorDoor() == other.getColorDoor()
                && Objects.equals(controlConfiguration, other.controlConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getHeight(), getWidth(), getDepth(),
                getUpperFrame(), getBottomFrame(), getDoorThickness(),
                getCountCells(), getTypeLb(), getDirectionDoorOpening(),
                getColorBody(), getColorDoor(), controlConfiguration);
    }

    @Override
    public String toString() {
        return "LBC{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", height=" + getHeight() +
                ", width=" + getWidth() +
                ", depth=" + getDepth() +
                ", upperFrame=" + getUpperFrame() +
                ", bottomFrame=" + getBottomFrame() +
                ", doorThickness=" + getDoorThickness() +
                ", typeLb=" + getTypeLb() +
                ", countCells=" + getCountCells() +
                ", cellHeight=" + getCellHeight() +
                ", cellWidth=" + getCellWidth() +
                ", cellDepth=" + getCellDepth() +
                ", directionDoorOpening=" + getDirectionDoorOpening() +
                ", colorBody=" + getColorBody() +
                ", colorDoor=" + getColorDoor() +
                ", controlConfiguration=" + controlConfiguration +
                '}';
    }
}
