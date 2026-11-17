package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDomainTest {

    @Test
    void persistedEmployees_shouldBeIdentifiedByStableId() {
        Employee first = employee(15L, "first@example.com");
        Employee second = employee(15L, "second@example.com");
        Employee another = employee(16L, "first@example.com");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, another);
    }

    @Test
    void transientEmployees_shouldNotBeEqualByMutableAttributes() {
        Employee first = employee(null, "same@example.com");
        Employee second = employee(null, "same@example.com");

        assertNotEquals(first, second);
    }

    @Test
    void changePersonalData_shouldNotChangeEmployeeIdentity() {
        Employee employee = employee(15L, "old@example.com");

        employee.changePersonalData(
                "Пётр",
                "Петров",
                "new@example.com"
        );

        assertEquals(15L, employee.getId());
        assertEquals("Пётр", employee.getFirstName());
        assertEquals("Петров", employee.getLastName());
        assertEquals("new@example.com", employee.getEmail());
    }

    @Test
    void changeRole_shouldUpdateRole() {
        Employee employee = employee(15L, "user@example.com");

        employee.changeRole(Role.ROLE_ADMIN);

        assertEquals(Role.ROLE_ADMIN, employee.getRole());
    }

    @Test
    void constructor_shouldRejectNonPositiveId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> employee(0L, "user@example.com")
        );
    }

    private Employee employee(Long id, String email) {
        return new Employee(
                id,
                "Иван",
                "Иванов",
                email,
                LocalDate.of(2026, 1, 10),
                Role.ROLE_MANAGER
        );
    }
}
