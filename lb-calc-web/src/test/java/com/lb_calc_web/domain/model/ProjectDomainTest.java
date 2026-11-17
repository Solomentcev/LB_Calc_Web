package com.lb_calc_web.domain.model;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверяет правила состава и производного описания проекта.
 */
class ProjectDomainTest {

    @Test
    void description_shouldNotDependOnAlsInsertionOrder() {
        Employee employee = new Employee(
                "Тест",
                "Тестов",
                "project-domain@example.test",
                LocalDate.of(2026, 1, 10),
                Role.ROLE_MANAGER
        );

        ALS lcConfiguration = TestDataFactory.validALS(1L);
        ALS lbcConfiguration = ALSDtoMapper.toDomain(
                TestDataFactory.validLBCALSDTO(2L)
        );

        Project first = new Project(
                "Проект",
                "Компания",
                LocalDate.of(2026, 1, 10),
                employee
        );
        first.addALS(lcConfiguration);
        first.addALS(lbcConfiguration);

        Project second = new Project(
                "Проект",
                "Компания",
                LocalDate.of(2026, 1, 10),
                employee
        );
        second.addALS(lbcConfiguration);
        second.addALS(lcConfiguration);

        assertEquals(first.getDescription(), second.getDescription());
    }
}
