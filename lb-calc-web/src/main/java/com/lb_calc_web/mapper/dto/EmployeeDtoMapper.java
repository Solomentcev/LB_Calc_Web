package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Маппер между Domain Employee и DTO.
 */
public final class EmployeeDtoMapper {

    private EmployeeDtoMapper() {
    }

    public static Employee toDomain(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Employee(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getRegistrationDate(),
                dto.getRole()
        );
    }

    public static EmployeeDTO toDto(Employee domain) {
        if (domain == null) {
            return null;
        }

        EmployeeDTO dto = new EmployeeDTO();

        dto.setFirstName(domain.getFirstName());
        dto.setLastName(domain.getLastName());
        dto.setEmail(domain.getEmail());
        dto.setRegistrationDate(domain.getRegistrationDate());
        dto.setRole(domain.getRole());

        /*
         * id и пароль отсутствуют в Domain Employee,
         * поэтому здесь они намеренно не заполняются.
         */
        return dto;
    }

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

    public static ProfileDTO toProfileDto(Employee domain) {
        Objects.requireNonNull(
                domain,
                "Employee не должен быть null"
        );

        ProfileDTO dto = new ProfileDTO();

        dto.setFirstName(domain.getFirstName());
        dto.setLastName(domain.getLastName());
        dto.setEmail(domain.getEmail());
        dto.setRegistrationDate(domain.getRegistrationDate());
        dto.setRole(domain.getRole());

        return dto;
    }

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