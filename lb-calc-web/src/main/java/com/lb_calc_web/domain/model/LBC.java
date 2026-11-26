package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;

import java.util.Map;
import java.util.Objects;

/**
 * Комбинированный модуль LBC с отдельной технической и складскими ячейками.
 *
 * <p>В {@link StorageModule#getCells()} входят только складские ячейки.
 * Техническая ячейка является отдельной физической областью и не участвует
 * в количестве и нумерации складских ячеек.</p>
 */
public class LBC extends StorageModule implements ControlModule {

    private static final int DEFAULT_TECHNICAL_CELL_MIN_WIDTH = 200;
    private static final int DEFAULT_TECHNICAL_CELL_MIN_HEIGHT = 200;
    private static final int DEFAULT_EQUIPMENT_CLEARANCE = 20;
    private static final int DEFAULT_DISPLAY_CENTER_HEIGHT = 1300;

    private final int technicalCellMinWidth;
    private final int technicalCellMinHeight;
    private final int equipmentClearance;
    private final int targetDisplayCenterHeight;
    private final int storageCellMinHeight;

    private ControlConfiguration controlConfiguration;
    private int technicalCellWidth;
    private int technicalCellHeight;
    private int technicalCellDepth;
    private int technicalCellTop;
    private int storageCellsAbove;
    private int storageCellsBelow;
    private int displayTop = -1;

    public LBC(int height, int width, int depth, int upperFrame, int bottomFrame,
               Colors colorBody, Colors colorDoor, TypeLb typeLb, int countCells,
               DirectionDoorOpening directionDoorOpening, int doorThickness,
               ControlConfiguration controlConfiguration) {
        this(height, width, depth, upperFrame, bottomFrame, colorBody, colorDoor,
                typeLb, countCells, directionDoorOpening, doorThickness,
                controlConfiguration, DEFAULT_TECHNICAL_CELL_MIN_WIDTH,
                DEFAULT_TECHNICAL_CELL_MIN_HEIGHT, DEFAULT_EQUIPMENT_CLEARANCE,
                DEFAULT_DISPLAY_CENTER_HEIGHT, 85);
    }

    public LBC(int height, int width, int depth, int upperFrame, int bottomFrame,
               Colors colorBody, Colors colorDoor, TypeLb typeLb, int countCells,
               DirectionDoorOpening directionDoorOpening, int doorThickness,
               ControlConfiguration controlConfiguration, int technicalCellMinWidth,
               int technicalCellMinHeight, int equipmentClearance,
               int targetDisplayCenterHeight) {
        this(height, width, depth, upperFrame, bottomFrame, colorBody, colorDoor,
                typeLb, countCells, directionDoorOpening, doorThickness, controlConfiguration,
                technicalCellMinWidth, technicalCellMinHeight, equipmentClearance,
                targetDisplayCenterHeight, 85);
    }

    public LBC(int height, int width, int depth, int upperFrame, int bottomFrame,
               Colors colorBody, Colors colorDoor, TypeLb typeLb, int countCells,
               DirectionDoorOpening directionDoorOpening, int doorThickness,
               ControlConfiguration controlConfiguration, int technicalCellMinWidth,
               int technicalCellMinHeight, int equipmentClearance,
               int targetDisplayCenterHeight, int storageCellMinHeight) {
        super(doorThickness);
        if (technicalCellMinWidth <= 0 || technicalCellMinHeight <= 0
                || equipmentClearance < 0 || targetDisplayCenterHeight < 0
                || storageCellMinHeight <= 0) {
            throw new IllegalArgumentException("Параметры геометрии LBC заданы некорректно");
        }
        this.technicalCellMinWidth = technicalCellMinWidth;
        this.technicalCellMinHeight = technicalCellMinHeight;
        this.equipmentClearance = equipmentClearance;
        this.targetDisplayCenterHeight = targetDisplayCenterHeight;
        this.storageCellMinHeight = storageCellMinHeight;
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
                controlConfiguration, "Конфигурация управления не должна быть null").copy();
        setCells(createCells(countCells));
        recalculate();
    }

    @Override
    public ControlConfiguration getControlConfiguration() {
        return controlConfiguration.copy();
    }

    @Override
    public void setFunctionalConfiguration(FunctionalConfiguration configuration) {
        controlConfiguration.setFunctionalConfiguration(configuration);
        recalculate();
    }

    @Override
    public void setControlConfiguration(ControlConfiguration controlConfiguration) {
        this.controlConfiguration = Objects.requireNonNull(
                controlConfiguration, "Конфигурация управления не должна быть null").copy();
        recalculate();
    }

    @Override
    public Map<Equipment, Integer> getEquipment() {
        return controlConfiguration.getEquipment();
    }

    @Override
    public void addEquipment(Equipment equipment, int quantity) {
        controlConfiguration.addEquipment(
                Objects.requireNonNull(equipment, "Оборудование не должно быть null"), quantity);
        recalculate();
    }

    @Override
    public void removeEquipment(Equipment equipment) {
        controlConfiguration.removeEquipment(
                Objects.requireNonNull(equipment, "Оборудование не должно быть null"));
        recalculate();
    }

    @Override
    public int getEquipmentHeight() {
        return getEquipment().keySet().stream().mapToInt(Equipment::getHeight).max().orElse(0);
    }

    /** Минимальная ширина технической ячейки с учётом дисплея и оборудования. */
    public int getTechnicalCellWidth() {
        return technicalCellWidth;
    }

    /** Рассчитанная высота технической ячейки, мм. */
    public int getTechnicalCellHeight() {
        return technicalCellHeight;
    }

    /** Полезная глубина технической ячейки, мм. */
    public int getTechnicalCellDepth() {
        return technicalCellDepth;
    }

    /** Координата верхней границы технической ячейки от верха корпуса, мм. */
    public int getTechnicalCellTop() {
        return technicalCellTop;
    }

    public int getStorageCellsAbove() {
        return storageCellsAbove;
    }

    public int getStorageCellsBelow() {
        return storageCellsBelow;
    }

    /** Координата верхней границы дисплея от верха корпуса, мм; -1 если дисплей не установлен. */
    public int getDisplayTop() {
        return displayTop;
    }

    /** Возвращает физическую модель дисплея, если она установлена. */
    public Equipment getDisplay() {
        return getEquipment().keySet().stream()
                .filter(item -> item.getCategory() == EquipmentCategory.DISPLAY)
                .findFirst().orElse(null);
    }

    @Override
    public void recalculateCells() {
        if (getTypeLb() == null) {
            throw new IllegalStateException("Тип модуля хранения не задан");
        }

        int count = getCountCells();
        long usableHeight = (long) getHeight() - getUpperFrame() - getBottomFrame();
        long shelfTotal = (long) count * getTypeLb().getShelfThick();
        long compartmentHeight = usableHeight - shelfTotal;

        Equipment display = getDisplay();
        long equipmentWidth = getEquipment().keySet().stream()
                .mapToLong(Equipment::getWidth).max().orElse(0);
        long equipmentHeight = getEquipment().keySet().stream()
                .mapToLong(Equipment::getHeight).max().orElse(0);
        long equipmentDepth = getEquipment().keySet().stream()
                .mapToLong(Equipment::getDepth).max().orElse(0);

        technicalCellWidth = saturatedInt(Math.max(
                technicalCellMinWidth,
                Math.max(equipmentWidth, display == null ? 0L
                        : (long) display.getWidth() + 2L * equipmentClearance)));
        technicalCellHeight = 0;
        technicalCellDepth = saturatedInt(Math.max(0L, (long) getDepth() - getDoorThickness()));
        technicalCellTop = getUpperFrame();
        storageCellsAbove = 0;
        storageCellsBelow = count;
        displayTop = -1;

        long minimumTechnicalHeight = Math.max(
                technicalCellMinHeight,
                display == null ? 0L : (long) display.getHeight() + 2L * equipmentClearance);
        minimumTechnicalHeight = Math.max(minimumTechnicalHeight, equipmentHeight);

        int selectedHeight = 0;
        int selectedBelow = count;
        int selectedAbove = 0;
        long selectedTechnicalHeight = minimumTechnicalHeight;
        long selectedCenterDistance = Long.MAX_VALUE;

        if (count >= 2 && compartmentHeight >= minimumTechnicalHeight
                + (long) count * boundsMinCellHeight()) {
            int bestImbalance = Integer.MAX_VALUE;
            for (int below = 1; below < count; below++) {
                int above = count - below;
                int imbalance = Math.abs(above - below);
                if (imbalance > bestImbalance) {
                    continue;
                }

                long maxStorageHeight = (compartmentHeight - minimumTechnicalHeight) / count;
                for (long cellHeight = boundsMinCellHeight();
                     cellHeight <= maxStorageHeight; cellHeight++) {
                    long technicalHeight = compartmentHeight - (long) count * cellHeight;
                    long technicalBottom = getBottomFrame()
                            + (long) below * (cellHeight + getTypeLb().getShelfThick());
                    long center = technicalBottom + technicalHeight / 2;
                    long distance = Math.abs(center - targetDisplayCenterHeight);

                    if (imbalance < bestImbalance || distance < selectedCenterDistance
                            || (distance == selectedCenterDistance && cellHeight > selectedHeight)) {
                        bestImbalance = imbalance;
                        selectedCenterDistance = distance;
                        selectedHeight = saturatedInt(cellHeight);
                        selectedTechnicalHeight = technicalHeight;
                        selectedBelow = below;
                        selectedAbove = above;
                    }
                }
            }
        }

        if (selectedAbove == 0 && selectedBelow == count) {
            if (count > 0) {
                long cellHeight = count == 0 ? 0 : Math.max(0, compartmentHeight
                        - minimumTechnicalHeight) / count;
                selectedHeight = saturatedInt(cellHeight);
                selectedTechnicalHeight = compartmentHeight - (long) count * cellHeight;
            } else {
                selectedHeight = 0;
                selectedTechnicalHeight = Math.max(0, compartmentHeight);
            }
        }

        technicalCellHeight = saturatedInt(selectedTechnicalHeight);
        storageCellsAbove = selectedAbove;
        storageCellsBelow = selectedBelow;
        long technicalBottom = getBottomFrame()
                + (long) selectedBelow * (selectedHeight + getTypeLb().getShelfThick());
        technicalCellTop = saturatedInt((long) getHeight() - technicalBottom
                - technicalCellHeight);

        // Ширина LBC должна обеспечивать минимальную ширину технической
        // и складской ячеек независимо от наличия физического дисплея.
        long requiredModuleWidth = (long) technicalCellWidth + getTypeLb().getDeltaWidth();
        setWidth(saturatedInt(Math.max(getWidth(), requiredModuleWidth)));

        int cellWidth = saturatedInt((long) getWidth() - getTypeLb().getDeltaWidth());
        int cellDepth = saturatedInt((long) getDepth() - getDoorThickness());
        setCellDimensions(selectedHeight, cellWidth, cellDepth);

        if (display != null) {
            displayTop = saturatedInt((long) technicalCellTop
                    + (technicalCellHeight - display.getHeight()) / 2);
        }
    }

    private int boundsMinCellHeight() {
        return storageCellMinHeight;
    }

    private int saturatedInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE
                : value < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) value;
    }

    @Override
    public void recalculate() {
        recalculateCells();
        setName(ModuleTextFormatter.combinedName(this, this));
        setDescription(ModuleTextFormatter.storageDescription(this, "Модуль хранения и управления")
                + "\n\n" + ModuleTextFormatter.controlDetailsDescription(this));
    }

    @Override
    public LBC copy() {
        LBC copy = new LBC(getHeight(), getWidth(), getDepth(), getUpperFrame(), getBottomFrame(),
                getColorBody(), getColorDoor(), getTypeLb(), getCountCells(),
                getDirectionDoorOpening(), getDoorThickness(), controlConfiguration,
                technicalCellMinWidth, technicalCellMinHeight, equipmentClearance,
                targetDisplayCenterHeight, storageCellMinHeight);
        copy.copyCellNumbersFrom(this);
        return copyTextTo(copyIdentityTo(copy));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LBC other = (LBC) object;
        return getHeight() == other.getHeight() && getWidth() == other.getWidth()
                && getDepth() == other.getDepth() && getUpperFrame() == other.getUpperFrame()
                && getBottomFrame() == other.getBottomFrame()
                && getDoorThickness() == other.getDoorThickness()
                && getCountCells() == other.getCountCells()
                && Objects.equals(getTypeLb(), other.getTypeLb())
                && getDirectionDoorOpening() == other.getDirectionDoorOpening()
                && getColorBody() == other.getColorBody() && getColorDoor() == other.getColorDoor()
                && Objects.equals(controlConfiguration, other.controlConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHeight(), getWidth(), getDepth(), getUpperFrame(), getBottomFrame(),
                getDoorThickness(), getCountCells(), getTypeLb(), getDirectionDoorOpening(),
                getColorBody(), getColorDoor(), controlConfiguration);
    }

    @Override
    public String toString() {
        return "LBC{name='" + getName() + "', height=" + getHeight() + ", width=" + getWidth()
                + ", depth=" + getDepth() + ", countCells=" + getCountCells()
                + ", cellHeight=" + getCellHeight() + ", technicalCellHeight=" + technicalCellHeight
                + ", technicalCellTop=" + technicalCellTop + ", controlConfiguration="
                + controlConfiguration + '}';
    }
}