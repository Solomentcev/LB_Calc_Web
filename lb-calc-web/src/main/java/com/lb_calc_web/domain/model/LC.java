package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;

import java.util.Map;
import java.util.Objects;

/**
 * Модуль управления LC.
 *
 * <p>LC является модулем управления и реализует интерфейс
 * {@link ControlModule}. Модуль предназначен для размещения оборудования,
 * необходимого для управления автоматизированной системой.</p>
 *
 * <p>При наличии установленного дисплея ширина LC и его положение
 * рассчитываются по физическим габаритам оборудования. Если дисплей
 * не установлен, сохраняется заданная ширина корпуса.</p>
 *
 * <p>Наименование и описание являются производными значениями
 * и формируются при пересчёте модуля.</p>
 */
public class LC extends Module implements ControlModule {

    private final LCGeometrySettings geometrySettings;

    /** Конфигурация функций управления LC. */
    private ControlConfiguration controlConfiguration;

    /** Создаёт LC с явно заданными настройками физических расчётов. */
    public LC(int height, int width, int depth, int upperFrame, int bottomFrame,
              Colors colorBody, Colors colorDoor,
              ControlConfiguration controlConfiguration,
              LCGeometrySettings geometrySettings) {
        this.geometrySettings = Objects.requireNonNull(
                geometrySettings, "Настройки геометрии LC не должны быть null");
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

    /**
     * Рассчитывает положение верхнего края дисплея относительно верхнего края LC.
     *
     * <p>Положение дисплея рассчитывается по геометрическим параметрам,
     * загруженным из конфигурации приложения.</p>
     *
     * @return координата верхнего края дисплея от верхнего края LC, мм;
     *         {@code 0}, если дисплей не установлен
     */
    public int getDisplayTop() {
        Equipment display = findDisplay();
        int displayHeight = display == null ? 0 : display.getHeight();
        if (displayHeight <= 0) {
            return 0;
        }

        // Координата задаётся в миллиметрах от верхнего края корпуса.
        // Округляем вниз, чтобы при нечётной высоте дисплея не сместить его центр ниже заданной отметки.
        int centeredTop = (int) Math.floor(
                getHeight() - geometrySettings.displayCenterHeight() - displayHeight / 2.0
        );
        return Math.max(centeredTop, getUpperFrame() + geometrySettings.displayTopClearance());
    }

    /** Пересчитывает производные характеристики LC. */
    @Override
    public void recalculate() {
        setWidth(calculateWidth());
        updateNameAndDescription();
    }

    /**
     * Возвращает минимальную ширину корпуса с учётом установленного дисплея.
     *
     * @return минимальная ширина без физического дисплея либо ширина,
     *         рассчитанная по габаритам дисплея и параметрам конфигурации
     */
    public int getRequiredWidth() {
        Equipment display = findDisplay();
        if (display == null) {
            return geometrySettings.minimumWidth();
        }

        long requiredWidth = Math.max(
                geometrySettings.minimumWidth(),
                (long) display.getWidth() + 2L * geometrySettings.displaySideClearance()
        );
        long roundedWidth = ((requiredWidth + geometrySettings.widthRoundingStep() - 1)
                / geometrySettings.widthRoundingStep()) * geometrySettings.widthRoundingStep();
        return Math.toIntExact(roundedWidth);
    }

    /**
     * Рассчитывает ширину корпуса LC.
     *
     * <p>Если физический дисплей не установлен, сохраняет заданную ширину,
     * ограничивая её конструктивным минимумом. При установке дисплея ширина
     * определяется его габаритами и установленными боковыми зазорами.</p>
     */
    private int calculateWidth() {
        if (findDisplay() == null) {
            return Math.max(geometrySettings.minimumWidth(), getWidth());
        }
        return getRequiredWidth();
    }

    private Equipment findDisplay() {
        return controlConfiguration.getEquipment().keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.DISPLAY)
                .findFirst()
                .orElse(null);
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
                getColorBody(), getColorDoor(), controlConfiguration, geometrySettings);
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
