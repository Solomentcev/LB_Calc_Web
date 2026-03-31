package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API для управления Сотрудниками
 * Только администраторы имеют доступ
 */
@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class EmployeeRestController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeRestController.class);
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;
    public EmployeeRestController(EmployeeService employeeService, PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * GET /api/v1/employees
     * Получить всех сотрудников (только для админа)
     */
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        logger.info("Fetching all employees");

        try {
            List<EmployeeDTO> employees = employeeService.findAll();
            ApiResponse<List<EmployeeDTO>> response=ApiResponse.success(employees);
                      return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching employees", e);
            ApiResponse<String> response=ApiResponse.error("Error fetching employees",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/v1/employees/{id}
     * Получить сотрудника по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        logger.info("Fetching employee with id: {}", id);

        try {
            EmployeeDTO employee = employeeService.loadUserById(id.intValue());
            ApiResponse<EmployeeDTO> response=ApiResponse.success(employee);
                       return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Employee not found with id: {}", id);
            ApiResponse<String> response=ApiResponse.error("Employee not found with id"+id,e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * POST /api/v1/employees
     * Создать нового сотрудника
     */
    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody @Valid CreateEmployeeDTO createEmployeeDTO) {
        logger.info("Creating new employee: {}", createEmployeeDTO.getEmail());

        try {
            if (employeeService.existsByEmail(createEmployeeDTO.getEmail())){
                ApiResponse<Void> apiResponse=ApiResponse.error("Пользователь с таким email уже существует");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }
            if (!createEmployeeDTO.getPassword().equals(createEmployeeDTO.getConfirmPassword())){
                ApiResponse<Void> apiResponse=ApiResponse.error("Пароли не совпадают");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }
            EmployeeDTO employeeDTO=new EmployeeDTO();
            employeeDTO.setFirstName(createEmployeeDTO.getFirstName());
            employeeDTO.setLastName(createEmployeeDTO.getLastName());
            employeeDTO.setEmail(createEmployeeDTO.getEmail());
            employeeDTO.setRole(createEmployeeDTO.getRole());
            employeeDTO.setPassword(createEmployeeDTO.getPassword());
            employeeDTO.setEncryptedPassword(passwordEncoder.encode(createEmployeeDTO.getPassword()));
            employeeDTO=employeeService.save(employeeDTO);
            ApiResponse<EmployeeDTO> response=ApiResponse.success(employeeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating employee", e);
            ApiResponse<String> response=ApiResponse.error("Error creating employee",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PUT /api/v1/employees/{id}
     * Обновить сотрудника
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable int id,
            @RequestBody @Valid ProfileDTO employeeUpd) {
        logger.info("Updating employee with id: {}", id);
        EmployeeDTO employeeDTO=employeeService.loadUserById(id);
        if (!employeeDTO.getEmail().equals(employeeUpd.getEmail())
                && employeeService.existsByEmail(employeeUpd.getEmail())){
            ApiResponse<Void> apiResponse=ApiResponse.error("Пользователь с таким email уже существует");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        try {
            employeeDTO.setFirstName(employeeUpd.getFirstName());
            employeeDTO.setLastName(employeeUpd.getLastName());
            employeeDTO.setEmail(employeeUpd.getEmail());
            employeeDTO.setRole(employeeUpd.getRole());
            employeeDTO=employeeService.save(employeeDTO);
            ApiResponse<EmployeeDTO> response=ApiResponse.success(employeeDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating employee", e);
            ApiResponse<String> response=ApiResponse.error("Error updating employee",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        }
    }

    /**
     * DELETE /api/v1/employees/{id}
     * Удалить сотрудника
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        logger.info("Deleting employee with id: {}", id);

        try {
            employeeService.deleteById(id);
            ApiResponse<Object> response=ApiResponse.success("Employee deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting employee", e);
            ApiResponse<String> response=ApiResponse.error("Error deleting employee",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
