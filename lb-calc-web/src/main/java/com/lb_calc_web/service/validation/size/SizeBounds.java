package com.lb_calc_web.service.validation.size;

import com.lb_calc_web.domain.model.ModuleGeometryBounds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Конфигурация размерных ограничений и значений по умолчанию модулей. */
@Component
public class SizeBounds implements ModuleGeometryBounds {

    private final int upperFrameMin;
    private final int upperFrameMax;
    private final int bottomFrameMin;
    private final int bottomFrameMax;
    private final int heightCellMin;
    private final int heightMax;
    private final int heightMin;
    private final int heightLcMin;
    private final int heightLcPanelMin;
    private final int countCellsMin;
    private final int countCellsMax;
    private final int widthCellMin;
    private final int widthMin;
    private final int widthMax;
    private final int depthCellMin;
    private final int depthCellMax;
    private final int depthMin;
    private final int depthMax;
    private final int doorThicknessMin;
    private final int doorThicknessMax;
    private final int typeDeltaWidthMin;
    private final int typeDeltaWidthMax;
    private final int typeShelfThickMin;
    private final int typeShelfThickMax;
    private final int typeServiceZoneWidthMin;
    private final int typeServiceZoneWidthMax;
    private final int heightDefault;
    private final int widthDefault;
    private final int depthDefault;
    private final int upperFrameDefault;
    private final int bottomFrameDefault;
    private final int countCellsDefault;
    private final int doorThicknessDefault;
    private final int lcMinimumWidth;
    private final int lcDisplaySideClearance;
    private final int lcWidthRoundingStep;
    private final int lcDisplayCenterHeight;
    private final int lcDisplayTopClearance;
    private final int lbcStorageCellMinWidth;
    private final int lbcTechnicalCellMinWidth;
    private final int lbcTechnicalCellMinHeight;
    private final int lbcEquipmentClearance;
    private final int lbcDisplayCenterHeight;

    public SizeBounds(
            @Value("${size.frame.upper.min}") int upperFrameMin,
            @Value("${size.frame.upper.max}") int upperFrameMax,
            @Value("${size.frame.bottom.min}") int bottomFrameMin,
            @Value("${size.frame.bottom.max}") int bottomFrameMax,
            @Value("${size.height.cell.min}") int heightCellMin,
            @Value("${size.height.max}") int heightMax,
            @Value("${size.height.min}") int heightMin,
            @Value("${size.height.lc.min}") int heightLcMin,
            @Value("${size.height.lc.panel.min}") int heightLcPanelMin,
            @Value("${size.count.cells.min}") int countCellsMin,
            @Value("${size.count.cells.max}") int countCellsMax,
            @Value("${size.width.cell.min}") int widthCellMin,
            @Value("${size.width.min}") int widthMin,
            @Value("${size.width.max}") int widthMax,
            @Value("${size.depth.cell.min}") int depthCellMin,
            @Value("${size.depth.cell.max}") int depthCellMax,
            @Value("${size.depth.min}") int depthMin,
            @Value("${size.depth.max}") int depthMax,
            @Value("${size.door.thickness.min}") int doorThicknessMin,
            @Value("${size.door.thickness.max}") int doorThicknessMax,
            @Value("${size.type.delta-width.min}") int typeDeltaWidthMin,
            @Value("${size.type.delta-width.max}") int typeDeltaWidthMax,
            @Value("${size.type.shelf-thick.min}") int typeShelfThickMin,
            @Value("${size.type.shelf-thick.max}") int typeShelfThickMax,
            @Value("${size.type.service-zone-width.min}") int typeServiceZoneWidthMin,
            @Value("${size.type.service-zone-width.max}") int typeServiceZoneWidthMax,
            @Value("${size.height.default}") int heightDefault,
            @Value("${size.width.default}") int widthDefault,
            @Value("${size.depth.default}") int depthDefault,
            @Value("${size.frame.upper.default}") int upperFrameDefault,
            @Value("${size.frame.bottom.default}") int bottomFrameDefault,
            @Value("${size.count.cells.default}") int countCellsDefault,
            @Value("${size.door.thickness.default}") int doorThicknessDefault,
            @Value("${lc.geometry.width.min}") int lcMinimumWidth,
            @Value("${lc.geometry.display.side-clearance}") int lcDisplaySideClearance,
            @Value("${lc.geometry.width.rounding-step}") int lcWidthRoundingStep,
            @Value("${lc.geometry.display.center-height}") int lcDisplayCenterHeight,
            @Value("${lc.geometry.display.top-clearance}") int lcDisplayTopClearance,
            @Value("${lbc.geometry.storage-cell.width.min}") int lbcStorageCellMinWidth,
            @Value("${lbc.geometry.technical-cell.width.min}") int lbcTechnicalCellMinWidth,
            @Value("${lbc.geometry.technical-cell.height.min}") int lbcTechnicalCellMinHeight,
            @Value("${lbc.geometry.equipment.clearance}") int lbcEquipmentClearance,
            @Value("${lbc.geometry.display.center-height}") int lbcDisplayCenterHeight
    ) {
        this.upperFrameMin = upperFrameMin;
        this.upperFrameMax = upperFrameMax;
        this.bottomFrameMin = bottomFrameMin;
        this.bottomFrameMax = bottomFrameMax;
        this.heightCellMin = heightCellMin;
        this.heightMax = heightMax;
        this.heightMin = heightMin;
        this.heightLcMin = heightLcMin;
        this.heightLcPanelMin = heightLcPanelMin;
        this.countCellsMin = countCellsMin;
        this.countCellsMax = countCellsMax;
        this.widthCellMin = widthCellMin;
        this.widthMin = widthMin;
        this.widthMax = widthMax;
        this.depthCellMin = depthCellMin;
        this.depthCellMax = depthCellMax;
        this.depthMin = depthMin;
        this.depthMax = depthMax;
        this.doorThicknessMin = doorThicknessMin;
        this.doorThicknessMax = doorThicknessMax;
        this.typeDeltaWidthMin = typeDeltaWidthMin;
        this.typeDeltaWidthMax = typeDeltaWidthMax;
        this.typeShelfThickMin = typeShelfThickMin;
        this.typeShelfThickMax = typeShelfThickMax;
        this.typeServiceZoneWidthMin = typeServiceZoneWidthMin;
        this.typeServiceZoneWidthMax = typeServiceZoneWidthMax;
        this.heightDefault = heightDefault;
        this.widthDefault = widthDefault;
        this.depthDefault = depthDefault;
        this.upperFrameDefault = upperFrameDefault;
        this.bottomFrameDefault = bottomFrameDefault;
        this.countCellsDefault = countCellsDefault;
        this.doorThicknessDefault = doorThicknessDefault;
        this.lcMinimumWidth = lcMinimumWidth;
        this.lcDisplaySideClearance = lcDisplaySideClearance;
        this.lcWidthRoundingStep = lcWidthRoundingStep;
        this.lcDisplayCenterHeight = lcDisplayCenterHeight;
        this.lcDisplayTopClearance = lcDisplayTopClearance;
        this.lbcStorageCellMinWidth = lbcStorageCellMinWidth;
        this.lbcTechnicalCellMinWidth = lbcTechnicalCellMinWidth;
        this.lbcTechnicalCellMinHeight = lbcTechnicalCellMinHeight;
        this.lbcEquipmentClearance = lbcEquipmentClearance;
        this.lbcDisplayCenterHeight = lbcDisplayCenterHeight;
        validateBounds();
    }

    /** Проверяет согласованность всех централизованных размерных ограничений. */
    private void validateBounds() {
        validateRange("верхняя рама", upperFrameMin, upperFrameMax);
        validateRange("нижняя рама", bottomFrameMin, bottomFrameMax);
        validateRange("высота корпуса", heightMin, heightMax);
        validateRange("глубина корпуса", depthMin, depthMax);
        validateRange("ширина корпуса", widthMin, widthMax);
        validateRange("глубина ячейки", depthCellMin, depthCellMax);
        validateRange("толщина двери", doorThicknessMin, doorThicknessMax);
        validateRange("добавочная ширина типа", typeDeltaWidthMin, typeDeltaWidthMax);
        validateRange("толщина полки", typeShelfThickMin, typeShelfThickMax);
        validateRange("ширина сервисной зоны", typeServiceZoneWidthMin, typeServiceZoneWidthMax);

        if (heightCellMin <= 0 || widthCellMin <= 0 || countCellsMin <= 0
                || countCellsMax < countCellsMin || lcMinimumWidth <= 0
                || lcDisplaySideClearance < 0 || lcWidthRoundingStep <= 0
                || lcDisplayCenterHeight < 0 || lcDisplayTopClearance < 0
                || lbcStorageCellMinWidth <= 0 || lbcTechnicalCellMinWidth <= 0
                || lbcTechnicalCellMinHeight <= 0 || lbcEquipmentClearance < 0
                || lbcDisplayCenterHeight < 0) {
            throw new IllegalArgumentException("Размерные ограничения содержат недопустимые значения");
        }
        validateDefault("высота", heightDefault, heightMin, heightMax);
        validateDefault("ширина", widthDefault, widthMin, widthMax);
        validateDefault("глубина", depthDefault, depthMin, depthMax);
        validateDefault("толщина двери", doorThicknessDefault, doorThicknessMin, doorThicknessMax);
        validateDefault("количество ячеек", countCellsDefault, countCellsMin, countCellsMax);
    }

    private static void validateRange(String name, int min, int max) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Некорректные границы параметра: " + name);
        }
    }

    private static void validateDefault(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("Значение по умолчанию вне допустимого диапазона: " + name);
        }
    }

    public int getLcMinimumWidth() {
        return lcMinimumWidth;
    }

    public int getLcDisplaySideClearance() {
        return lcDisplaySideClearance;
    }

    public int getLcWidthRoundingStep() {
        return lcWidthRoundingStep;
    }

    public int getLcDisplayCenterHeight() {
        return lcDisplayCenterHeight;
    }

    public int getLcDisplayTopClearance() {
        return lcDisplayTopClearance;
    }

    public int getLbcStorageCellMinWidth() { return lbcStorageCellMinWidth; }

    public int getLbcTechnicalCellMinWidth() { return lbcTechnicalCellMinWidth; }

    public int getLbcTechnicalCellMinHeight() { return lbcTechnicalCellMinHeight; }

    public int getLbcEquipmentClearance() { return lbcEquipmentClearance; }

    public int getLbcDisplayCenterHeight() { return lbcDisplayCenterHeight; }

    public int getUpperFrameMin() {
        return upperFrameMin;
    }

    public int getUpperFrameMax() {
        return upperFrameMax;
    }

    public int getBottomFrameMin() {
        return bottomFrameMin;
    }

    public int getBottomFrameMax() {
        return bottomFrameMax;
    }

    public int getHeightCellMin() {
        return heightCellMin;
    }

    public int getHeightMax() {
        return heightMax;
    }

    public int getHeightMin() {
        return heightMin;
    }

    public int getHeightLcMin() {
        return heightLcMin;
    }

    public int getHeightLcPanelMin() {
        return heightLcPanelMin;
    }

    public int getCountCellsMin() {
        return countCellsMin;
    }

    public int getCountCellsMax() {
        return countCellsMax;
    }

    public int getWidthCellMin() {
        return widthCellMin;
    }

    public int getWidthMin() {
        return widthMin;
    }

    public int getWidthMax() {
        return widthMax;
    }

    public int getDepthCellMin() {
        return depthCellMin;
    }

    public int getDepthCellMax() {
        return depthCellMax;
    }

    public int getDepthMin() {
        return depthMin;
    }

    public int getDepthMax() {
        return depthMax;
    }

    public int getDoorThicknessMin() {
        return doorThicknessMin;
    }

    public int getDoorThicknessMax() {
        return doorThicknessMax;
    }

    public int getTypeDeltaWidthMin() {
        return typeDeltaWidthMin;
    }

    public int getTypeDeltaWidthMax() {
        return typeDeltaWidthMax;
    }

    public int getTypeShelfThickMin() {
        return typeShelfThickMin;
    }

    public int getTypeShelfThickMax() {
        return typeShelfThickMax;
    }

    public int getTypeServiceZoneWidthMin() {
        return typeServiceZoneWidthMin;
    }

    public int getTypeServiceZoneWidthMax() {
        return typeServiceZoneWidthMax;
    }

    /** Возвращает значение по умолчанию: heightDefault. */
    public int getHeightDefault() {
        return heightDefault;
    }

    /** Возвращает значение по умолчанию: widthDefault. */
    public int getWidthDefault() {
        return widthDefault;
    }

    /** Возвращает значение по умолчанию: depthDefault. */
    public int getDepthDefault() {
        return depthDefault;
    }

    /** Возвращает значение по умолчанию: upperFrameDefault. */
    public int getUpperFrameDefault() {
        return upperFrameDefault;
    }

    /** Возвращает значение по умолчанию: bottomFrameDefault. */
    public int getBottomFrameDefault() {
        return bottomFrameDefault;
    }

    /** Возвращает значение по умолчанию: countCellsDefault. */
    public int getCountCellsDefault() {
        return countCellsDefault;
    }

    /** Возвращает значение по умолчанию: doorThicknessDefault. */
    public int getDoorThicknessDefault() {
        return doorThicknessDefault;
    }


}
