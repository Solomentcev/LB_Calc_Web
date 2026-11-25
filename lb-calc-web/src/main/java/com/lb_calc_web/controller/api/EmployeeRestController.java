package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ChangePasswordDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class EmployeeRestController {

    private final EmployeeService employeeService;
    private final AuthService authService;

    public EmployeeRestController(
            EmployeeService employeeService,
            AuthService authService
    ) {
        this.employeeService = employeeService;
        this.authService = authService;
    }

    @GetMapping
    /** Возвращает список учётных записей сотрудников. */
    public ResponseEntity<?> getAllEmployees() {

            List<EmployeeDTO> employees =
                    employeeService.findAll();

            return ResponseEntity.ok(
                    ApiResponse.success(employees)
            );

    }

    /**
     * Получить состояние активности учётных записей сотрудников.
     */
    @GetMapping("/statuses")
    public ResponseEntity<?> getEmployeeStatuses() {
        return ResponseEntity.ok(
                ApiResponse.success(employeeService.getActiveStatuses())
        );
    }

    @GetMapping("/{id}")
    /** Возвращает сотрудника по идентификатору. */
    public ResponseEntity<?> getEmployeeById(
            @PathVariable Long id
    ) {

            EmployeeDTO employee =
                    employeeService.loadUserById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(employee)
            );

    }

    @PostMapping
    /** Создаёт учётную запись сотрудника. */
    public ResponseEntity<?> createEmployee(
            @RequestBody
            @Valid
            CreateEmployeeDTO createEmployeeDTO
    ) {

            EmployeeDTO employee =
                    authService.createEmployee(
                            createEmployeeDTO
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    employee
                            )
                    );

    }

    @PutMapping("/{id}")
    /** Обновляет данные учётной записи сотрудника. */
    public ResponseEntity<?> updateEmployee(
            @PathVariable int id,
            @RequestBody @Valid ProfileDTO employeeUpd
    ) {

            EmployeeDTO employee =
                    employeeService.loadUserById(id);

            if (!employee.getEmail().equals(
                    employeeUpd.getEmail()
            )
                    && employeeService.existsByEmail(
                    employeeUpd.getEmail()
            )) {

                throw new IllegalArgumentException(
                        "Пользователь с таким email уже существует"
                );
            }

            employee.setFirstName(
                    employeeUpd.getFirstName()
            );

            employee.setLastName(
                    employeeUpd.getLastName()
            );

            employee.setEmail(
                    employeeUpd.getEmail()
            );

            employee.setRole(
                    employeeUpd.getRole()
            );

            EmployeeDTO saved =
                    employeeService.save(employee);

            return ResponseEntity.ok(
                    ApiResponse.success(saved)
            );

    }

    @PostMapping("/{id}/activate")
    /** Активирует учётную запись сотрудника. */
    public ResponseEntity<?> activateEmployee(
            @PathVariable Long id
    ) {

            employeeService.activateById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Employee activated successfully")
            );

    }

    @DeleteMapping("/{id}")
    /** Деактивирует или удаляет учётную запись сотрудника согласно правилам сервиса. */
    public ResponseEntity<?> deleteEmployee(
            @PathVariable Long id
    ) {

            employeeService.deactivateById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Employee deactivated successfully"
                    )
            );

    }

    /**
     * Получить профиль текущего сотрудника.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentProfile() {

            return ResponseEntity.ok(
                    ApiResponse.success(employeeService.getCurrentEmployee())
            );

    }

    /**
     * Получить список профилей сотрудников.
     */
    @GetMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllProfiles() {
        return ResponseEntity.ok(
                ApiResponse.success(employeeService.getAllProfiles())
        );
    }

    /**
     * Получить профиль сотрудника по ID.
     */
    @GetMapping("/profiles/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfileById(
            @PathVariable int id
    ) {

            return ResponseEntity.ok(
                    ApiResponse.success(employeeService.getProfileById(id))
            );

    }

    /**
     * Изменить пароль текущего сотрудника.
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeCurrentPassword(
            @RequestBody @Valid ChangePasswordDTO passwordDTO
    ) {
        if (!passwordDTO.getPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

            EmployeeDTO currentEmployee = employeeService.getCurrentEmployee();
            authService.changePassword(currentEmployee.getId(), passwordDTO.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Пароль успешно изменён")
            );

    }

    /**
     * Изменить пароль сотрудника от имени администратора.
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changeEmployeePassword(
            @PathVariable Long id,
            @RequestBody @Valid ChangePasswordDTO passwordDTO
    ) {
        if (!passwordDTO.getPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

            authService.changePassword(id, passwordDTO.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Пароль успешно изменён")
            );

    }

}