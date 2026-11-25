package com.lb_calc_web.service;

import java.time.LocalDate;

/** Независимые параметры фильтрации списка проектов. */
public record ProjectCatalogFilter(
        Long id,
        String name,
        String company,
        String description,
        String createdByFirstName,
        String createdByLastName,
        LocalDate createdAtFrom,
        LocalDate createdAtTo,
        String updatedByFirstName,
        String updatedByLastName,
        LocalDate updatedAtFrom,
        LocalDate updatedAtTo
) {
}
