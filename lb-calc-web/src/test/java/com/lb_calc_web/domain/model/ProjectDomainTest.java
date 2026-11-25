package com.lb_calc_web.domain.model;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет правила состава и производного описания проекта.
 */
class ProjectDomainTest {

    @Test
    void replaceALS_shouldChangeOnlyOneInstanceAndRecalculateDescription() {
        Employee employee = new Employee(
                "Тест",
                "Тестов",
                "project-replacement@example.test",
                LocalDate.of(2026, 1, 10),
                Role.ROLE_MANAGER
        );
        ALS original = TestDataFactory.validALS(1L);
        ALS replacement = ALSDtoMapper.toDomain(
                TestDataFactory.validLBCALSDTO(2L)
        );
        Project project = new Project(
                "Проект",
                "Компания",
                LocalDate.of(2026, 1, 10),
                employee
        );
        project.addALS(original);
        project.addALS(original);
        String previousDescription = project.getDescription();

        project.replaceALS(original, replacement);

        assertEquals(1, project.getQuantity(original));
        assertEquals(1, project.getQuantity(replacement));
        assertTrue(!project.getDescription().equals(previousDescription));
        assertTrue(project.getDescription().contains(replacement.getName()));
    }

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
        second.addALS(lcConfiguration);

        assertEquals(first.getDescription(), second.getDescription());
        assertEquals(2, first.getDescription().lines()
                .filter(line -> line.matches("\\d+\\. .* — \\d+ шт\\."))
                .count());
        assertTrue(first.getDescription().contains("6 ячеек в каждой АКХ."));
        assertTrue(first.getDescription().contains("3 ячейки в каждой АКХ."));
        assertTrue(first.getDescription().contains("Всего: 3 АКХ, 15 ячеек."));
        assertTrue(!first.getDescription().contains("Состав модулей слева направо:"));
    }
}
