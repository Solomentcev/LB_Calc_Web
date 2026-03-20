package com.lb_calc_web.service;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.mapper.EmployeeMapper;
import com.lb_calc_web.model.user.Employee;
import com.lb_calc_web.repository.EmployeeRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class EmployeeService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;

    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        logger.info("loadUserByUsername");
        logger.info("email: " + email);
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Пользователь %s не найден".formatted(email)));
        logger.debug("loadUserByUsername: {}", employee);
        return EmployeeMapper.toEmployeeDTO(employee);
    }

    public EmployeeDTO loadUserByEmail(String email) {
        logger.info("Поиск пользователя (%s)...".formatted(email));
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Пользователь %s не найден".formatted(email)));
        logger.debug(employee.toString());
        EmployeeDTO employeeDTO = EmployeeMapper.toEmployeeDTO(employee);
        return employeeDTO;
    }

    public EmployeeDTO loadUserById(int id) {
        logger.info("Поиск пользователя(id%d)...".formatted(id));
        Employee employee = employeeRepository.findById((long) id).orElseThrow(() ->
                new NoSuchElementException("Пользователь с id%d не найден".formatted(id)));
        return EmployeeMapper.toEmployeeDTO(employee);
    }


    // @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDTO save(EmployeeDTO employeeDTO) {
        if (employeeDTO.getId() == 0) {
            logger.info("Сохранение пользователя (%s)...".formatted(employeeDTO.getEmail()));
            employeeDTO.setRegistrationDate(LocalDate.now());
        }
        else {
            logger.info("Обновление пользователя (%s)...".formatted(employeeDTO.getEmail()));
        }
        logger.debug(employeeDTO.toString());
        logger.debug(employeeDTO.getPassword() + " " + employeeDTO.getEncryptedPassword());
        Employee employee = EmployeeMapper.toEmployee(employeeDTO);
        employeeRepository.save(employee);
        logger.debug(employee.getPassword());
        return EmployeeMapper.toEmployeeDTO(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        logger.info("Удаление пользователя(id%d)...".formatted(id));
        employeeRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(EmployeeDTO employeeDTO) {
        logger.info("Удаление пользователя(%s)...".formatted(employeeDTO.getEmail()));
        Employee employee = EmployeeMapper.toEmployee(employeeDTO);
        employeeRepository.delete(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<EmployeeDTO> findAll() {
        logger.info("Получение списка пользователей...");
        List<Employee> employees = employeeRepository.findAll();
        employees.sort(Comparator.comparing(Employee::getId));
        return EmployeeMapper.toEmployeeDTOList(employees);
    }

    public boolean existsByEmail(@NotBlank @Size(max = 50) @Email String email) {
        boolean isExist = employeeRepository.existsByEmail(email);
        logger.info(isExist? "Пользователь (%s) уже существует".formatted(email)
                    : "Пользователь (%s) не существует".formatted(email));
        return isExist;
    }
}

