package com.lb_calc_web.domain.geometry;

/** Контракт централизованных размерных ограничений и значений по умолчанию. */
public interface SizeBounds {

    int getLcMinimumWidth();
    int getLcDisplaySideClearance();
    int getLcWidthRoundingStep();
    int getLcDisplayCenterHeight();
    int getLcDisplayTopClearance();
    int getLbcStorageCellMinWidth();
    int getLbcTechnicalCellMinWidth();
    int getLbcTechnicalCellMinHeight();
    int getLbcEquipmentClearance();
    int getLbcDisplayCenterHeight();

    int getUpperFrameMin();
    int getUpperFrameMax();
    int getBottomFrameMin();
    int getBottomFrameMax();
    int getHeightCellMin();
    int getHeightMax();
    int getHeightMin();
    int getHeightLcMin();
    int getHeightLcPanelMin();
    int getCountCellsMin();
    int getCountCellsMax();
    int getWidthCellMin();
    int getWidthMin();
    int getWidthMax();
    int getDepthCellMin();
    int getDepthCellMax();
    int getDepthMin();
    int getDepthMax();
    int getDoorThicknessMin();
    int getDoorThicknessMax();

    int getHeightDefault();
    int getWidthDefault();
    int getDepthDefault();
    int getUpperFrameDefault();
    int getBottomFrameDefault();
    int getCountCellsDefault();
    int getDoorThicknessDefault();
}
