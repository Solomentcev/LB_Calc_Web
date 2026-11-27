package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Маппер между доменной моделью Employee и DTO.
 */
public final class EmployeeDtoMapper {

    private EmployeeDtoMapper() {
    }

    /** Преобразует DTO сотрудника в доменную модель. */
    public static Employee toDomain(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Employee(
                dto.getId(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getRegistrationDate(),
                dto.getRole()
        );
    }

    /** Преобразует DTO создания учётной записи в доменную модель сотрудника. */
    public static Employee toDomain(CreateEmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Employee(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                java.time.LocalDate.now(),
                dto.getRole()
        );
    }

    /** Преобразует доменную модель сотрудника в DTO. */
    public static EmployeeDTO toDto(Employee domain) {
        if (domain == null) {
            return null;
        }

        EmployeeDTO dto = new EmployeeDTO();

        dto.setId(domain.getId());
        dto.setFirstName(domain.getFirstName());
        dto.setLastName(domain.getLastName());
        dto.setEmail(domain.getEmail());
        dto.setRegistrationDate(domain.getRegistrationDate());
        dto.setRole(domain.getRole());

        return dto;
    }

    /** Преобразует список доменных сотрудников в список DTO. */
    public static List<EmployeeDTO> toDtoList(
            List<Employee> employees
    ) {
        if (employees == null) {
            return List.of();
        }

        List<EmployeeDTO> result = new ArrayList<>();

        for (Employee employee : employees) {
            result.add(toDto(employee));
        }

        return result;
    }

    /** Преобразует данные сотрудника в DTO профиля. */
    public static ProfileDTO toProfileDto(Employee domain) {
        Objects.requireNonNull(
                domain,
                "Employee не должен быть null"
        );

        ProfileDTO dto = new ProfileDTO();

        dto.setId(domain.getId());
        dto.setFirstName(domain.getFirstName());
        dto.setLastName(domain.getLastName());
        dto.setEmail(domain.getEmail());
        dto.setRegistrationDate(domain.getRegistrationDate());
        dto.setRole(domain.getRole());

        return dto;
    }

    /** Преобразует список сотрудников в список DTO профилей. */
    public static List<ProfileDTO> toProfileDtoList(
            List<Employee> employees
    ) {
        if (employees == null) {
            return List.of();
        }

        List<ProfileDTO> result = new ArrayList<>();

        for (Employee employee : employees) {
            result.add(toProfileDto(employee));
        }

        return result;
    }
}
