package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ChangePasswordDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class EmployeeRestController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    EmployeeRestController.class
            );

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
    public ResponseEntity<?> getAllEmployees() {

        try {

            List<EmployeeDTO> employees =
                    employeeService.findAll();

            return ResponseEntity.ok(
                    ApiResponse.success(employees)
            );

        } catch (Exception e) {

            logger.error(
                    "Ошибка получения сотрудников",
                    e
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            ApiResponse.error(
                                    "Ошибка получения сотрудников",
                                    e.getMessage()
                            )
                    );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(
            @PathVariable Long id
    ) {

        try {

            EmployeeDTO employee =
                    employeeService.loadUserById(
                            id.intValue()
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(employee)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "Сотрудник не найден",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(
            @RequestBody
            @Valid
            CreateEmployeeDTO createEmployeeDTO
    ) {

        try {

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

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            logger.error(
                    "Ошибка создания сотрудника",
                    e
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            ApiResponse.error(
                                    "Ошибка создания сотрудника",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable int id,
            @RequestBody @Valid ProfileDTO employeeUpd
    ) {

        try {

            EmployeeDTO employee =
                    employeeService.loadUserById(id);

            if (!employee.getEmail().equals(
                    employeeUpd.getEmail()
            )
                    && employeeService.existsByEmail(
                    employeeUpd.getEmail()
            )) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.error(
                                        "Пользователь с таким email уже существует"
                                )
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

        } catch (Exception e) {

            logger.error(
                    "Ошибка обновления сотрудника",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.error(
                                    "Ошибка обновления сотрудника",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateEmployee(
            @PathVariable Long id
    ) {
        try {
            employeeService.activateById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Employee activated successfully")
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Сотрудник не найден", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(
            @PathVariable Long id
    ) {

        try {

            employeeService.deactivateById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Employee deactivated successfully"
                    )
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "Сотрудник не найден",
                                    e.getMessage()
                            )
                    );
        } catch (Exception e) {

            logger.error(
                    "Ошибка деактивации сотрудника id={}",
                    id,
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка деактивации сотрудника",
                                    e.getMessage()
                            )
                    );
        }
    }

    /**
     * Получить профиль текущего сотрудника.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentProfile() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(employeeService.getCurrentEmployee())
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
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
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(employeeService.getProfileById(id))
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Сотрудник не найден", e.getMessage()));
        }
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
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Пароли не совпадают"));
        }

        try {
            EmployeeDTO currentEmployee = employeeService.getCurrentEmployee();
            authService.changePassword(currentEmployee.getId(), passwordDTO.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Пароль успешно изменён")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
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
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Пароли не совпадают"));
        }

        try {
            authService.changePassword(id, passwordDTO.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Пароль успешно изменён")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

}