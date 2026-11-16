package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.attributes.TypeLb;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectDomainTest {

    @Test
    void replaceALS_shouldReplaceOnlyOneUnit() {
        Employee employee = new Employee(
                "Иван",
                "Иванов",
                "ivan@example.com",
                LocalDate.of(2026, 1, 1),
                Role.ROLE_MANAGER
        );

        Project project = new Project(
                "Проект",
                "Компания",
                LocalDate.of(2026, 1, 1),
                employee
        );

        ALS original = createALS(10);
        ALS replacement = createALS(9);

        project.addALS(original);
        project.addALS(original);
        project.addALS(original);
        project.addALS(replacement);
        project.addALS(replacement);

        project.replaceALS(original, replacement);

        assertEquals(2, project.getQuantity(original));
        assertEquals(3, project.getQuantity(replacement));
    }

    private ALS createALS(int countCells) {
        ControlConfiguration control = new ControlConfiguration(
                Payment.NONE,
                Set.<AccessMethod>of(),
                Set.<PrintOption>of(),
                new EquipmentConfiguration(List.of(), List.of())
        );

        LBC lbc = new LBC(
                1940,
                500,
                600,
                100,
                100,
                Colors.Blue,
                Colors.White,
                new TypeLb("TYPE1", 100, 5, 20),
                countCells,
                DirectionDoorOpening.LEFT,
                20,
                control
        );

        return new ALS(
                1940,
                600,
                100,
                100,
                Colors.Blue,
                Colors.White,
                List.of(lbc)
        );
    }
}
