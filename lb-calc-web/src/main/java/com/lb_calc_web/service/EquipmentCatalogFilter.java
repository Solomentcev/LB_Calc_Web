package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;

/**
 * Набор независимых фильтров каталога физических моделей оборудования.
 *
 * @param name название модели, поиск по вхождению
 * @param functionalType функциональный тип
 * @param category физическая категория
 * @param purpose назначение, поиск по вхождению
 * @param widthMin минимальная ширина
 * @param widthMax максимальная ширина
 * @param heightMin минимальная высота
 * @param heightMax максимальная высота
 * @param depthMin минимальная глубина
 * @param depthMax максимальная глубина
 * @param description описание, поиск по вхождению
 */
public record EquipmentCatalogFilter(
        String name,
        EquipmentFunctionalType functionalType,
        EquipmentCategory category,
        String purpose,
        Integer widthMin,
        Integer widthMax,
        Integer heightMin,
        Integer heightMax,
        Integer depthMin,
        Integer depthMax,
        String description
) {
}
