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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST-контроллер управления сотрудниками и их профилями. */
@RestController
@Validated
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Сотрудники", description = "Управление учётными записями и профилями сотрудников")
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Некорректный запрос или ошибка валидации",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Требуется аутентификация",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Недостаточно прав для выполнения операции",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Запрашиваемый ресурс не найден",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "405",
        description = "HTTP-метод не поддерживается",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "Конфликт текущего состояния данных",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "415",
        description = "Формат содержимого запроса не поддерживается",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "Внутренняя ошибка сервера",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
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
    @Operation(summary = "Получить список сотрудников")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getAllEmployees() {

            List<EmployeeDTO> employees =
                    EmployeeDtoMapper.toDtoList(employeeService.findAll());

            return ResponseEntity.ok(
                    ApiResponse.success(employees)
            );

    }

    /**
     * Получить состояние активности учётных записей сотрудников.
     */
    @Operation(summary = "Получить статусы сотрудников")
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<java.util.Map<Long, Boolean>>> getEmployeeStatuses() {
        return ResponseEntity.ok(
                ApiResponse.success(employeeService.getActiveStatuses())
        );
    }

    /** Возвращает сотрудника по идентификатору. */
    @Operation(summary = "Получить сотрудника по идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(
            @PathVariable @Positive Long id
    ) {

            EmployeeDTO employee =
                    EmployeeDtoMapper.toDto(employeeService.loadUserById(id));

            return ResponseEntity.ok(
                    ApiResponse.success(employee)
            );

    }

    /** Создаёт учётную запись сотрудника. */
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Сотрудник создан")
    @Operation(summary = "Создать сотрудника")
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(
            @RequestBody
            @Valid
            CreateEmployeeDTO createEmployeeDTO
    ) {

            EmployeeDTO employee = EmployeeDtoMapper.toDto(authService.createEmployee(
                    createEmployeeDTO.getFirstName(),
                    createEmployeeDTO.getLastName(),
                    createEmployeeDTO.getEmail(),
                    createEmployeeDTO.getRole(),
                    createEmployeeDTO.getPassword(),
                    createEmployeeDTO.getConfirmPassword()
            ));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    employee
                            )
                    );

    }

    /** Обновляет данные учётной записи сотрудника. */
    @Operation(summary = "Обновить данные сотрудника")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @PathVariable @Positive int id,
            @RequestBody @Valid ProfileDTO employeeUpd
    ) {

            Employee employee = employeeService.loadUserById(id);

            if (!employee.getEmail().equals(
                    employeeUpd.getEmail()
            )
                    && employeeService.existsByEmail(
                    employeeUpd.getEmail()
            )) {

                throw new IllegalStateException(
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
    @Operation(summary = "Активировать сотрудника")
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateEmployee(
            @PathVariable @Positive Long id
    ) {

            employeeService.activateById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Employee activated successfully")
            );

    }

    /** Деактивирует или удаляет учётную запись сотрудника согласно правилам сервиса. */
    @Operation(summary = "Деактивировать сотрудника")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable @Positive Long id
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
    @Operation(summary = "Получить свой профиль")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getCurrentProfile() {

            return ResponseEntity.ok(
                    ApiResponse.success(EmployeeDtoMapper.toDto(employeeService.getCurrentEmployee()))
            );

    }

    /**
     * Получить список профилей сотрудников.
     */
    @Operation(summary = "Получить список профилей")
    @GetMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProfileDTO>>> getAllProfiles() {
        return ResponseEntity.ok(
                ApiResponse.success(EmployeeDtoMapper.toProfileDtoList(employeeService.findAllProfiles()))
        );
    }

    /**
     * Получить профиль сотрудника по ID.
     */
    @Operation(summary = "Получить профиль по идентификатору")
    @GetMapping("/profiles/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileDTO>> getProfileById(
            @PathVariable @Positive int id
    ) {

            return ResponseEntity.ok(
                    ApiResponse.success(EmployeeDtoMapper.toProfileDto(employeeService.getProfileById(id)))
            );

    }

    /**
     * Изменить пароль текущего сотрудника.
     */
    @Operation(summary = "Изменить свой пароль")
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changeCurrentPassword(
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
    @Operation(summary = "Изменить пароль сотрудника")
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changeEmployeePassword(
            @PathVariable @Positive Long id,
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