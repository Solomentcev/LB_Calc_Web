package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(
            @PathVariable Long id
    ) {

        try {

            employeeService.deleteById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Employee deleted successfully"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            ApiResponse.error(
                                    "Ошибка удаления сотрудника",
                                    e.getMessage()
                            )
                    );
        }
    }
}