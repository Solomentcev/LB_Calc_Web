package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;

import java.util.Objects;

/**
 * Модуль хранения LB.
 *
 * <p>LB предназначен для хранения ячеек и не выполняет функцию
 * управления. Конструктивные параметры модуля задаются при создании,
 * а производные характеристики, включая размеры ячеек, рассчитываются
 * методом {@link #recalculate()}.</p>
 *
 * <p>Количество ячеек определяется фактическим количеством объектов
 * {@link StorageCell} в модуле. Номера ячеек не относятся к конфигурации
 * самого LB и назначаются агрегатом {@link ALS} в зависимости от
 * положения модуля в ALS.</p>
 */
public class LB extends StorageModule {

    /**
     * Создаёт модуль хранения LB с заданной конфигурацией.
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
     */
    public LB(
            int height,
            int width,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor,
            TypeLb typeLb,
            int countCells,
            DirectionDoorOpening directionDoorOpening,
            int doorThickness
    ) {
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
        setCells(createCells(countCells));

        recalculate();
    }

    /**
     * Пересчитывает размеры ячеек LB.
     */
    @Override
    public void recalculateCells() {
        recalculateStorageCells();
    }

    /**
     * Пересчитывает производные характеристики LB.
     *
     * <p>Пересчитываются размеры ячеек, после чего формируются
     * наименование и описание модуля.</p>
     */
    @Override
    public void recalculate() {
        recalculateCells();

        setName(
                "Модуль хранения на "
                        + getCountCells()
                        + " ячеек"
        );

        setDescription(String.format(
                "Модуль хранения на %d ячеек тип-%s "
                        + "(%.1fx%dx%d), ВхШхГ,мм: %dx%dx%d, "
                        + "%s, %s/%s, толщина дверцы %d мм",
                getCountCells(),
                getTypeLb().getType(),
                (double) getCellHeight(),
                getCellWidth(),
                getCellDepth(),
                getHeight(),
                getWidth(),
                getDepth(),
                getDirectionDoorOpening(),
                getColorBody(),
                getColorDoor(),
                getDoorThickness()
        ));
    }

    /**
     * Сравнивает LB по исходной конфигурации.
     *
     * <p>В сравнении участвуют только параметры, определяющие
     * конфигурацию модуля. Наименование, описание и рассчитанные
     * размеры ячеек не учитываются.</p>
     *
     * <p>Номера {@link StorageCell} также не участвуют в сравнении,
     * поскольку назначаются конкретным ALS.</p>
     *
     * @param o объект для сравнения
     * @return {@code true}, если конфигурации LB совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LB other = (LB) o;

        return getHeight() == other.getHeight()
                && getWidth() == other.getWidth()
                && getDepth() == other.getDepth()
                && getUpperFrame() == other.getUpperFrame()
                && getBottomFrame() == other.getBottomFrame()
                && getDoorThickness() == other.getDoorThickness()
                && getCountCells() == other.getCountCells()
                && Objects.equals(getTypeLb(), other.getTypeLb())
                && getDirectionDoorOpening()
                == other.getDirectionDoorOpening()
                && getColorBody() == other.getColorBody()
                && getColorDoor() == other.getColorDoor();
    }

    /**
     * Возвращает хэш-код конфигурации LB.
     *
     * @return хэш-код конфигурации
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                getHeight(),
                getWidth(),
                getDepth(),
                getUpperFrame(),
                getBottomFrame(),
                getDoorThickness(),
                getCountCells(),
                getTypeLb(),
                getDirectionDoorOpening(),
                getColorBody(),
                getColorDoor()
        );
    }

    /**
     * Возвращает подробное строковое представление LB.
     *
     * @return строковое представление конфигурации и рассчитанных
     *         параметров LB
     */
    @Override
    public String toString() {
        return "LB{" +
                "name='" + getName() + '\'' +
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
                ", directionDoorOpening=" +
                getDirectionDoorOpening() +
                ", colorBody=" + getColorBody() +
                ", colorDoor=" + getColorDoor() +
                '}';
    }
}
