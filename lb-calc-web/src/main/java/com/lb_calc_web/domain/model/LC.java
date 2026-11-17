package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;
import java.util.Objects;

/**
 * Модуль управления LC.
 *
 * <p>LC является модулем управления и реализует интерфейс
 * {@link ControlModule}. Модуль предназначен для размещения оборудования,
 * необходимого для управления автоматизированной системой.</p>
 *
 * <p>Ширина LC задаётся явно, но не может быть меньше минимальной ширины,
 * необходимой для установленного оборудования. Минимальная требуемая
 * ширина определяется максимальной шириной оборудования.</p>
 *
 * <p>Наименование и описание являются производными значениями
 * и формируются при пересчёте модуля.</p>
 */
public class LC extends Module implements ControlModule {

    /**
     * Конфигурация функций управления LC.
     */
    private ControlConfiguration controlConfiguration;

    /**
     * Создаёт модуль управления LC с заданной конфигурацией.
     *
     * @param height высота модуля, мм
     * @param width ширина модуля, мм
     * @param depth глубина модуля, мм
     * @param upperFrame размер верхней рамы, мм
     * @param bottomFrame размер нижней рамы, мм
     * @param colorBody цвет корпуса
     * @param colorDoor цвет дверей
     * @param controlConfiguration конфигурация управления
     * @throws NullPointerException если цвет корпуса, цвет дверей
     *                              или конфигурация управления не заданы
     */
    public LC(
            int height,
            int width,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor,
            ControlConfiguration controlConfiguration
    ) {
        setHeight(height);
        setWidth(width);
        setDepth(depth);
        setUpperFrame(upperFrame);
        setBottomFrame(bottomFrame);

        setColorBody(colorBody);
        setColorDoor(colorDoor);

        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration,
                "Конфигурация управления не должна быть null"
        ).copy();

        recalculate();
    }

    /**
     * Возвращает независимую копию конфигурации управления LC.
     *
     * <p>Изменение возвращённого объекта не влияет на состояние модуля.
     * Для изменения конфигурации следует использовать методы самого модуля.</p>
     *
     * @return копия конфигурации управления
     */
    @Override
    public ControlConfiguration getControlConfiguration() {
        return controlConfiguration.copy();
    }

    /**
     * Заменяет конфигурацию управления LC.
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
        ).copy();

        recalculate();
    }

    /**
     * Возвращает всё оборудование, входящее в конфигурацию LC.
     *
     * @return список обязательного и дополнительного оборудования
     */
    @Override
    public List<Equipment> getEquipment() {
        return controlConfiguration.getAllEquipment();
    }

    /**
     * Добавляет оборудование в конфигурацию LC.
     *
     * <p>Добавляемое оборудование помещается в список дополнительного
     * оборудования.</p>
     *
     * @param equipment добавляемое оборудование
     * @throws NullPointerException если оборудование не задано
     */
    @Override
    public void addEquipment(Equipment equipment) {
        Objects.requireNonNull(
                equipment,
                "Оборудование не должно быть null"
        );

        controlConfiguration.addAdditionalEquipment(equipment);

        recalculate();
    }

    /**
     * Удаляет оборудование из конфигурации LC.
     *
     * <p>Оборудование удаляется как из обязательного, так и из
     * дополнительного набора.</p>
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

        controlConfiguration.removeEquipment(equipment);

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
     * Пересчитывает производные характеристики LC.
     *
     * <p>При пересчёте обновляются производные наименование и описание.
     * Допустимость геометрических параметров проверяется валидатором.</p>
     */
    @Override
    public void recalculate() {
        updateNameAndDescription();
    }

    /**
     * Обновляет производные наименование и описание LC.
     */
    private void updateNameAndDescription() {
        Display display = getEquipment().stream()
                .filter(Display.class::isInstance)
                .map(Display.class::cast)
                .findFirst()
                .orElse(null);

        String displayName = display != null
                ? display.getName()
                : "Без дисплея";

        String controlDescription = getControlDescription();

        setName("Модуль управления " + displayName);

        setDescription(
                "Модуль управления " + displayName + ";\n"
                        + "Размеры (ВхШхГ, мм): "
                        + getHeight() + "x"
                        + getWidth() + "x"
                        + getDepth() + ";\n"
                        + "Рамы (верх/низ, мм): "
                        + getUpperFrame() + "/"
                        + getBottomFrame() + ";\n"
                        + controlDescription + ";\n"
                        + "Цвет корпуса: " + getColorBody() + ";\n"
                        + "Цвет дверей: " + getColorDoor()
        );
    }

    /**
     * Создаёт независимую копию LC.
     *
     * @return копия LC с независимой конфигурацией управления
     */
    @Override
    public LC copy() {
        return new LC(
                getHeight(),
                getWidth(),
                getDepth(),
                getUpperFrame(),
                getBottomFrame(),
                getColorBody(),
                getColorDoor(),
                controlConfiguration
        );
    }

    /**
     * Сравнивает LC по его конфигурации.
     *
     * <p>В сравнении участвуют физические параметры модуля,
     * цвета и конфигурация управления. Наименование и описание
     * не участвуют, поскольку являются производными значениями.</p>
     *
     * @param o объект для сравнения
     * @return {@code true}, если конфигурации LC совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LC other = (LC) o;

        return getHeight() == other.getHeight()
                && getWidth() == other.getWidth()
                && getDepth() == other.getDepth()
                && getUpperFrame() == other.getUpperFrame()
                && getBottomFrame() == other.getBottomFrame()
                && getColorBody() == other.getColorBody()
                && getColorDoor() == other.getColorDoor()
                && Objects.equals(
                controlConfiguration,
                other.controlConfiguration
        );
    }

    /**
     * Возвращает хэш-код конфигурации LC.
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
                getColorBody(),
                getColorDoor(),
                controlConfiguration
        );
    }

    /**
     * Возвращает подробное строковое представление LC.
     *
     * @return строковое представление конфигурации LC
     */
    @Override
    public String toString() {
        return "LC{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", height=" + getHeight() +
                ", width=" + getWidth() +
                ", depth=" + getDepth() +
                ", upperFrame=" + getUpperFrame() +
                ", bottomFrame=" + getBottomFrame() +
                ", colorBody=" + getColorBody() +
                ", colorDoor=" + getColorDoor() +
                ", controlConfiguration=" +
                controlConfiguration +
                '}';
    }
}