package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Equipment;

import java.util.Map;
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

    /** Конфигурация функций управления LC. */
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
     */
    public LC(int height, int width, int depth, int upperFrame, int bottomFrame,
              Colors colorBody, Colors colorDoor,
              ControlConfiguration controlConfiguration) {
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

    /** @return независимая копия конфигурации управления LC */
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
     * Заменяет конфигурацию управления LC.
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

    /** @return позиции оборудования LC */
    @Override
    public Map<Equipment, Integer> getEquipment() {
        return controlConfiguration.getEquipment();
    }

    /**
     * Добавляет позицию оборудования в конфигурацию LC.
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
     * Удаляет позицию оборудования из конфигурации LC.
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

    /** Пересчитывает производные характеристики LC. */
    @Override
    public void recalculate() {
        updateNameAndDescription();
    }

    private void updateNameAndDescription() {
        setName(ModuleTextFormatter.controlName(this, this));
        setDescription(ModuleTextFormatter.controlDescription(this, this));
    }

    /** @return независимая копия LC */
    @Override
    public LC copy() {
        LC copy = new LC(
                getHeight(), getWidth(), getDepth(),
                getUpperFrame(), getBottomFrame(),
                getColorBody(), getColorDoor(), controlConfiguration);
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
        LC other = (LC) object;
        return getHeight() == other.getHeight()
                && getWidth() == other.getWidth()
                && getDepth() == other.getDepth()
                && getUpperFrame() == other.getUpperFrame()
                && getBottomFrame() == other.getBottomFrame()
                && getColorBody() == other.getColorBody()
                && getColorDoor() == other.getColorDoor()
                && Objects.equals(controlConfiguration, other.controlConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getHeight(), getWidth(), getDepth(),
                getUpperFrame(), getBottomFrame(),
                getColorBody(), getColorDoor(), controlConfiguration);
    }

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
                ", controlConfiguration=" + controlConfiguration +
                '}';
    }
}
