package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ChangePasswordDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST-контроллер управления сотрудниками и их профилями. */
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

    /** Возвращает список учётных записей сотрудников. */
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {

            List<EmployeeDTO> employees =
                    EmployeeDtoMapper.toDtoList(employeeService.findAll());

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

    /** Возвращает сотрудника по идентификатору. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(
            @PathVariable Long id
    ) {

            EmployeeDTO employee =
                    EmployeeDtoMapper.toDto(employeeService.loadUserById(id));

            return ResponseEntity.ok(
                    ApiResponse.success(employee)
            );

    }

    /** Создаёт учётную запись сотрудника. */
    @PostMapping
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

    /** Обновляет данные учётной записи сотрудника. */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable int id,
            @RequestBody @Valid ProfileDTO employeeUpd
    ) {

            Employee employee = employeeService.loadUserById(id);

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

            employee.changePersonalData(
                    employeeUpd.getFirstName(),
                    employeeUpd.getLastName(),
                    employeeUpd.getEmail()
            );
            employee.changeRole(employeeUpd.getRole());

            EmployeeDTO saved = EmployeeDtoMapper.toDto(employeeService.save(employee));

            return ResponseEntity.ok(
                    ApiResponse.success(saved)
            );

    }

    /** Активирует учётную запись сотрудника. */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateEmployee(
            @PathVariable Long id
    ) {

            employeeService.activateById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Employee activated successfully")
            );

    }

    /** Деактивирует или удаляет учётную запись сотрудника согласно правилам сервиса. */
    @DeleteMapping("/{id}")
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
                    ApiResponse.success(EmployeeDtoMapper.toDto(employeeService.getCurrentEmployee()))
            );

    }

    /**
     * Получить список профилей сотрудников.
     */
    @GetMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllProfiles() {
        return ResponseEntity.ok(
                ApiResponse.success(EmployeeDtoMapper.toProfileDtoList(employeeService.getAllProfiles()))
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
                    ApiResponse.success(EmployeeDtoMapper.toProfileDto(employeeService.getProfileById(id)))
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

            Employee currentEmployee = employeeService.getCurrentEmployee();
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