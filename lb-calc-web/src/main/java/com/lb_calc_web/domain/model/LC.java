package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        setColorBody(
                Objects.requireNonNull(
                        colorBody,
                        "Цвет корпуса не должен быть null"
                )
        );

        setColorDoor(
                Objects.requireNonNull(
                        colorDoor,
                        "Цвет дверей не должен быть null"
                )
        );

        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration,
                "Конфигурация управления не должна быть null"
        );

        recalculate();
    }

    /**
     * Возвращает конфигурацию управления LC.
     *
     * @return конфигурация управления
     */
    @Override
    public ControlConfiguration getControlConfiguration() {
        return controlConfiguration;
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
        );

        recalculate();
    }

    /**
     * Возвращает всё оборудование, входящее в конфигурацию LC.
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

        controlConfiguration
                .getEquipmentConfiguration()
                .addAdditionalEquipment(equipment);

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
     * Пересчитывает производные характеристики LC.
     *
     * <p>При пересчёте проверяется, что заданная ширина LC достаточна
     * для установленного оборудования, после чего обновляются
     * наименование и описание.</p>
     *
     * @throws IllegalStateException если ширина LC меньше минимально
     *                               необходимой ширины оборудования
     */
    @Override
    public void recalculate() {
        validateWidth();
        updateNameAndDescription();
    }

    /**
     * Проверяет достаточность заданной ширины LC.
     *
     * <p>Ширина LC может быть больше требуемой ширины оборудования,
     * но не может быть меньше неё.</p>
     *
     * @throws IllegalStateException если заданная ширина недостаточна
     */
    private void validateWidth() {
        int requiredWidth = getEquipment().stream()
                .mapToInt(Equipment::getWidth)
                .max()
                .orElse(0);

        if (getWidth() < requiredWidth) {
            throw new IllegalStateException(
                    String.format(
                            "Ширина LC %d мм меньше минимально необходимой "
                                    + "ширины оборудования %d мм",
                            getWidth(),
                            requiredWidth
                    )
            );
        }
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

        String accessMethods = controlConfiguration
                .getAccessMethods()
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ", "[", "]"));

        String printOptions = controlConfiguration
                .getPrintOptions()
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ", "[", "]"));

        String equipment = getEquipment().stream()
                .map(Equipment::getName)
                .collect(Collectors.joining(", "));

        Payment payment = controlConfiguration.getPayment();

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
                        + "Способы доступа: " + accessMethods + ";\n"
                        + "Печать: " + printOptions + ";\n"
                        + "Оплата: " + payment + ";\n"
                        + "Оборудование: " + equipment + ";\n"
                        + "Цвет корпуса: " + getColorBody() + ";\n"
                        + "Цвет дверей: " + getColorDoor()
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
                getControlConfiguration(),
                other.getControlConfiguration()
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
                getControlConfiguration()
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
                getControlConfiguration() +
                '}';
    }
}