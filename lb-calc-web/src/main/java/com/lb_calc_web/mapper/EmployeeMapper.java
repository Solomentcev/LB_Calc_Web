package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.model.user.Employee;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMapper {
    public static Employee toEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        if (employeeDTO.getId() != null && employeeDTO.getId() > 0) {
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
    public static List<ProfileDTO> toProfileDTOList(List<Employee> employees) {
        List<ProfileDTO> profileDTOS = new ArrayList<>();
        for (Employee employee : employees) {
            profileDTOS.add(toProfileDTOfromEmployee(employee));
        }
        return profileDTOS;
    }
    public static ProfileDTO toProfileDTOfromEmployee(Employee employee) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(employee.getId());
        profileDTO.setFirstName(employee.getFirstName());
        profileDTO.setLastName(employee.getLastName());
        profileDTO.setEmail(employee.getEmail());
        profileDTO.setRole(employee.getRole());
        profileDTO.setRegistrationDate(employee.getRegistrationDate());
        return profileDTO;
    }
    public static ProfileDTO toProfileDTOfromEmployeeDTO(EmployeeDTO employeeDTO) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(employeeDTO.getId());
        profileDTO.setFirstName(employeeDTO.getFirstName());
        profileDTO.setLastName(employeeDTO.getLastName());
        profileDTO.setEmail(employeeDTO.getEmail());
        profileDTO.setRole(employeeDTO.getRole());
        profileDTO.setRegistrationDate(employeeDTO.getRegistrationDate());
        return profileDTO;
    }
}
