package com.lb_calc_web.service.validation.size;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Набор граничных значений и ограничений геометрических параметров модулей. */
@Component
public class SizeBounds {

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
            @Value("${size.type.service-zone-width.max}") int typeServiceZoneWidthMax
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
    }

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

}
