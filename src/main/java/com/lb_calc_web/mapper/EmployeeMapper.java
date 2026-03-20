package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.model.user.Employee;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMapper {
    public static Employee toEmployee(EmployeeDTO employeeDTO) {
        if (employeeDTO == null) {
            return null;
        }
        Employee employee = new Employee();
        if (employeeDTO.getId() != 0) {
            employee.setId(employeeDTO.getId());
        }

        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPassword(employeeDTO.getEncryptedPassword());
        employee.setRole(employeeDTO.getRole());
        employee.setRegistrationDate(employeeDTO.getRegistrationDate());
        return employee;
    }
    public static EmployeeDTO toEmployeeDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());
        employeeDTO.setFirstName(employee.getFirstName());
        employeeDTO.setLastName(employee.getLastName());
        employeeDTO.setEmail(employee.getEmail());

        employeeDTO.setEncryptedPassword(employee.getPassword());
        employeeDTO.setRole(employee.getRole());
        employeeDTO.setRegistrationDate(employee.getRegistrationDate());
        return employeeDTO;
    }

    public static List<EmployeeDTO> toEmployeeDTOList(List<Employee> employees) {
        List<EmployeeDTO> employeeDTOS = new ArrayList<>();
        for (Employee employee : employees) {
            employeeDTOS.add(toEmployeeDTO(employee));
        }
        return employeeDTOS;
    }
}
