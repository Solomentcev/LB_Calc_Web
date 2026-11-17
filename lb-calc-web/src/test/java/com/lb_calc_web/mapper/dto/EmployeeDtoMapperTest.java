package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDtoMapperTest {

    @Test
    void toDomain_shouldPreserveEmployeeId() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(42L);
        dto.setFirstName("Иван");
        dto.setLastName("Иванов");
        dto.setEmail("ivanov@example.com");
        dto.setRegistrationDate(LocalDate.of(2026, 1, 10));
        dto.setRole(Role.ROLE_MANAGER);

        Employee domain = EmployeeDtoMapper.toDomain(dto);

        assertEquals(42L, domain.getId());
        assertEquals("ivanov@example.com", domain.getEmail());
    }

    @Test
    void toDto_shouldPreserveEmployeeId() {
        Employee domain = employee(42L);

        EmployeeDTO dto = EmployeeDtoMapper.toDto(domain);

        assertEquals(42L, dto.getId());
        assertEquals(domain.getEmail(), dto.getEmail());
    }

    @Test
    void toProfileDto_shouldPreserveEmployeeId() {
        Employee domain = employee(42L);

        ProfileDTO dto = EmployeeDtoMapper.toProfileDto(domain);

        assertEquals(42L, dto.getId());
        assertEquals(domain.getEmail(), dto.getEmail());
    }

    private Employee employee(Long id) {
        return new Employee(
                id,
                "Иван",
                "Иванов",
                "ivanov@example.com",
                LocalDate.of(2026, 1, 10),
                Role.ROLE_MANAGER
        );
    }
}
