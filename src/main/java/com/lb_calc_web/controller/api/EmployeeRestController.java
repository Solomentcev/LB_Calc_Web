package com.lb_calc_web.controller.api;

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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public EmployeeRestController(EmployeeService employeeService) {
        this.employeeService = employeeService;
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

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", employees);
            response.put("count", employees.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching employees", e);
            return buildErrorResponse("Failed to fetch employees", HttpStatus.INTERNAL_SERVER_ERROR);
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

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", employee);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Employee not found with id: {}", id);
            return buildErrorResponse("Employee not found", HttpStatus.NOT_FOUND);
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
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setEmail(createEmployeeDTO.getEmail());
            employeeDTO.setFirstName(createEmployeeDTO.getFirstName());
            employeeDTO.setLastName(createEmployeeDTO.getLastName());
            employeeDTO.setRole(createEmployeeDTO.getRole());

            EmployeeDTO createdEmployee = employeeService.save(employeeDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Employee created successfully");
            response.put("data", createdEmployee);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating employee", e);
            return buildErrorResponse("Failed to create employee: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * PUT /api/v1/employees/{id}
     * Обновить сотрудника
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeDTO employeeDTO) {
        logger.info("Updating employee with id: {}", id);

        try {
            employeeDTO.setId(id);
            EmployeeDTO updatedEmployee = employeeService.save(employeeDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Employee updated successfully");
            response.put("data", updatedEmployee);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating employee", e);
            return buildErrorResponse("Failed to update employee", HttpStatus.BAD_REQUEST);
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

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Employee deleted successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting employee", e);
            return buildErrorResponse("Failed to delete employee", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Вспомогательный метод для построения ошибки
     */
    private ResponseEntity<?> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(status).body(errorResponse);
    }
}
