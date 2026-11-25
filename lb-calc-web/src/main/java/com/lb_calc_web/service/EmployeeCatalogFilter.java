package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Role;

/** Независимые параметры фильтрации списков сотрудников и профилей. */
public record EmployeeCatalogFilter(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Boolean active,
        java.time.LocalDate registrationDateFrom,
        java.time.LocalDate registrationDateTo
) {
}
