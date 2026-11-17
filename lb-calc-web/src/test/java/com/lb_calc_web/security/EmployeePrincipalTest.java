package com.lb_calc_web.security;

import com.lb_calc_web.domain.attributes.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверки отображения состояния учётной записи в Spring Security.
 */
class EmployeePrincipalTest {

    @Test
    void isEnabled_shouldReturnTrueForActiveEmployee() {
        EmployeePrincipal principal = new EmployeePrincipal(
                1L,
                "active@example.com",
                "HASH",
                Role.ROLE_MANAGER,
                true
        );

        assertTrue(principal.isEnabled());
    }

    @Test
    void isEnabled_shouldReturnFalseForDeactivatedEmployee() {
        EmployeePrincipal principal = new EmployeePrincipal(
                1L,
                "inactive@example.com",
                "HASH",
                Role.ROLE_MANAGER,
                false
        );

        assertFalse(principal.isEnabled());
    }
}
