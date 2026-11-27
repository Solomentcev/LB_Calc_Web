package com.lb_calc_web.domain.model;

/**
 * Общие ограничения геометрии модулей управления и комбинированных модулей.
 *
 * <p>Реализация предоставляется конфигурацией приложения. Доменные модели
 * используют один общий экземпляр и не хранят копии отдельных числовых параметров.</p>
 */
public interface ModuleGeometryBounds {
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
    int getHeightCellMin();
}