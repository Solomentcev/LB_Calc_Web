package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;
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

    /**
     * Конфигурация функций управления LBC.
     */
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
    public LBC(
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
            int doorThickness,
            ControlConfiguration controlConfiguration
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

        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration,
                "Конфигурация управления не должна быть null"
        );

        setCells(createCells(countCells));

        recalculate();
    }

    /**
     * Возвращает конфигурацию управления LBC.
     *
     * @return конфигурация управления
     */
    @Override
    public ControlConfiguration getControlConfiguration() {
        return controlConfiguration;
    }

    /**
     * Заменяет конфигурацию управления LBC.
     *
     * @param controlConfiguration новая конфигурация управления
     * @throws NullPointerException если конфигурация не задана
     */
    public void setControlConfiguration(
            ControlConfiguration controlConfiguration
    ) {
        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration,
                "Конфигурация управления не должна быть null"
        );

        recalculate();
    }

    /**
     * Возвращает всё оборудование LBC.
     *
     * @return список обязательного и дополнительного оборудования
     */
    @Override
    public List<Equipment> getEquipment() {
        return controlConfiguration
                .getEquipmentConfiguration()
                .getAllEquipment();
    }

    /**
     * Добавляет оборудование в конфигурацию управления.
     *
     * <p>Добавленное оборудование считается дополнительным.</p>
     *
     * @param equipment добавляемое оборудование
     * @throws NullPointerException если оборудование не задано
     */
    @Override
    public void addEquipment(Equipment equipment) {
        controlConfiguration
                .getEquipmentConfiguration()
                .addAdditionalEquipment(
                        Objects.requireNonNull(
                                equipment,
                                "Оборудование не должно быть null"
                        )
                );

        recalculate();
    }

    /**
     * Удаляет оборудование из конфигурации управления.
     *
     * <p>Оборудование удаляется как из обязательного,
     * так и из дополнительного набора.</p>
     *
     * @param equipment удаляемое оборудование
     * @throws NullPointerException если оборудование не задано
     */
    @Override
    public void removeEquipment(Equipment equipment) {
        Objects.requireNonNull(
                equipment,
                "Оборудование не должно быть null"
        );

        controlConfiguration
                .getEquipmentConfiguration()
                .removeAdditionalEquipment(equipment);

        controlConfiguration
                .getEquipmentConfiguration()
                .removeRequiredEquipment(equipment);

        recalculate();
    }

    /**
     * Возвращает максимальную высоту установленного оборудования.
     *
     * <p>Если оборудование отсутствует, возвращается {@code 0}.</p>
     *
     * @return максимальная высота оборудования, мм
     */
    @Override
    public int getEquipmentHeight() {
        return getEquipment().stream()
                .mapToInt(Equipment::getHeight)
                .max()
                .orElse(0);
    }

    /**
     * Пересчитывает размеры ячеек LBC.
     */
    @Override
    public void recalculateCells() {
        recalculateStorageCells();
    }

    /**
     * Пересчитывает производные характеристики LBC.
     *
     * <p>Пересчитываются размеры ячеек, после чего формируются
     * наименование и описание модуля.</p>
     */
    @Override
    public void recalculate() {
        recalculateCells();

        setName(
                "Комбинированный модуль на "
                        + getCountCells()
                        + " ячеек"
        );

        setDescription(String.format(
                "Комбинированный модуль на %d ячеек тип-%s "
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
     * Сравнивает LBC по его конфигурации.
     *
     * <p>В сравнении участвуют параметры, определяющие конфигурацию
     * модуля, включая направление открытия дверей и конфигурацию
     * управления.</p>
     *
     * <p>Имя, описание и рассчитанные размеры ячеек не участвуют
     * в сравнении, поскольку являются производными значениями.</p>
     *
     * <p>Номера {@link StorageCell} также не участвуют в сравнении,
     * поскольку назначаются конкретным ALS.</p>
     *
     * @param o объект для сравнения
     * @return {@code true}, если конфигурации LBC совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LBC other = (LBC) o;

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
                && getColorDoor() == other.getColorDoor()
                && Objects.equals(
                getControlConfiguration(),
                other.getControlConfiguration()
        );
    }

    /**
     * Возвращает хэш-код конфигурации LBC.
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
                getColorDoor(),
                getControlConfiguration()
        );
    }

    /**
     * Возвращает подробное строковое представление LBC.
     *
     * @return строковое представление конфигурации LBC
     */
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
                ", directionDoorOpening=" +
                getDirectionDoorOpening() +
                ", colorBody=" + getColorBody() +
                ", colorDoor=" + getColorDoor() +
                ", controlConfiguration=" +
                getControlConfiguration() +
                '}';
    }
}