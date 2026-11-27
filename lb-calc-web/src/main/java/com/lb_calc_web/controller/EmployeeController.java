package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ChangePasswordDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeCatalogFilter;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** MVC-контроллер управления учётными записями сотрудников. */
@Controller
public class EmployeeController {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeController.class);

    private final List<Role> roles;
    private final EmployeeService employeeService;
    private final AuthService authService;

    public EmployeeController(
            EmployeeService employeeService,
            AuthService authService
    ) {
        this.employeeService = employeeService;
        this.authService = authService;
        this.roles = Arrays.asList(Role.values());
    }

    /** Подготавливает список ролей для форм управления сотрудниками. */
    @ModelAttribute
    public void addRoles(Model model) {
        model.addAttribute("roles", roles);
    }

    /** Отображает профиль текущего аутентифицированного сотрудника. */
    @GetMapping("/myprofile")
    public String myProfile(Model model) {

        EmployeeDTO employee = EmployeeDtoMapper.toDto(employeeService.getCurrentEmployee());

        model.addAttribute(
                "employee",
                employee
        );

        return "myprofile";
    }

    /** Отображает форму смены пароля текущего сотрудника. */
    @GetMapping("/myprofile/changepassword")
    public String changePasswordForm(
            Model model
    ) {

        model.addAttribute(
                "changePassword",
                new ChangePasswordDTO()
        );

        model.addAttribute(
                "action",
                "myprofile/changepassword"
        );

        return "change_password";
    }

    /** Проверяет данные и изменяет пароль текущего сотрудника. */
    @PostMapping("/myprofile/changepassword")
    public String changePassword(
            @ModelAttribute("changePassword")
            @Valid
            ChangePasswordDTO changePasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        model.addAttribute(
                "changePassword",
                changePasswordDTO
        );

        model.addAttribute(
                "action",
                "myprofile/changepassword"
        );

        validatePasswordConfirmation(
                changePasswordDTO,
                bindingResult
        );

        if (bindingResult.hasErrors()) {
            return "change_password";
        }

        try {

            EmployeeDTO employee = EmployeeDtoMapper.toDto(employeeService.getCurrentEmployee());

            authService.changePassword(
                    employee.getId(),
                    changePasswordDTO.getPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Пароль успешно заменен"
            );

            return "redirect:/myprofile";

        } catch (IllegalArgumentException exception) {
            model.addAttribute(
                    "error",
                    "Проверьте введённый пароль."
            );
            return "change_password";
        }
    }

    /** Отображает форму изменения пароля выбранного сотрудника. */
    @GetMapping("/employees/{id}/changepassword")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String changeEmployeePasswordForm(
            @PathVariable int id,
            Model model
    ) {

        model.addAttribute(
                "changePassword",
                new ChangePasswordDTO()
        );

        model.addAttribute(
                "id",
                id
        );

        model.addAttribute(
                "action",
                "/employees/" + id +
                        "/changepassword"
        );

        return "change_passwordEmployee";
    }

    /** Изменяет пароль выбранного сотрудника. */
    @PostMapping("/employees/{id}/changepassword")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String changeEmployeePassword(
            @PathVariable int id,
            @ModelAttribute("changePassword")
            @Valid
            ChangePasswordDTO changePasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        model.addAttribute(
                "changePassword",
                changePasswordDTO
        );

        validatePasswordConfirmation(
                changePasswordDTO,
                bindingResult
        );

        if (bindingResult.hasErrors()) {
            return "change_passwordEmployee";
        }

        try {

            EmployeeDTO employee = EmployeeDtoMapper.toDto(employeeService.loadUserById(id));

            authService.changePassword(
                    employee.getId(),
                    changePasswordDTO.getPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Пароль успешно заменен"
            );

            return "redirect:/employees/" +
                    employee.getId();

        } catch (IllegalArgumentException exception) {
            model.addAttribute(
                    "error",
                    "Проверьте введённый пароль."
            );
            return "change_passwordEmployee";
        }
    }

    /** Отображает страницу профилей сотрудников с фильтрацией и пагинацией. */
    @GetMapping("/profiles")
    public String profiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) LocalDate registrationDateFrom,
            @RequestParam(required = false) LocalDate registrationDateTo,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "firstName", "lastName", "email", "registrationDate"));
        EmployeeCatalogFilter filter = new EmployeeCatalogFilter(
                id, firstName, lastName, email, null, null, registrationDateFrom, registrationDateTo);
        var result = employeeService.findProfilesPage(filter, pageable);
        model.addAttribute("employees", result.getContent());
        model.addAttribute("filters", filter);
        CatalogPageSupport.addModelAttributes(model, result, pageable, "/profiles",
                CatalogPageSupport.sortOptions("id", "ID", "firstName", "Имя",
                        "lastName", "Фамилия", "email", "Электронная почта",
                        "registrationDate", "Дата регистрации"));
        return "profiles/profiles";
    }

    /** Отображает профиль выбранного сотрудника. */
    @GetMapping("/profiles/{id}")
    public String getProfile(
            @PathVariable int id,
            Model model
    ) {

        ProfileDTO user = EmployeeDtoMapper.toProfileDto(employeeService.getProfileById(id));

        model.addAttribute(
                "employee",
                user
        );

        model.addAttribute(
                "roles",
                roles
        );

        model.addAttribute(
                "id",
                user.getId()
        );

        return "profiles/profile";
    }

    /** Отображает список сотрудников с фильтрацией и пагинацией. */
    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String employees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) LocalDate registrationDateFrom,
            @RequestParam(required = false) LocalDate registrationDateTo,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "firstName", "lastName", "email", "role", "active", "registrationDate"));
        EmployeeCatalogFilter filter = new EmployeeCatalogFilter(
                id, firstName, lastName, email, role, active,
                registrationDateFrom, registrationDateTo);
        var result = employeeService.findProfilesPage(filter, pageable);
        model.addAttribute("employees", result.getContent());
        model.addAttribute("filters", filter);
        model.addAttribute("employeeStatuses", employeeService.getActiveStatuses(
                result.getContent().stream().map(Employee::getId).toList()));
        CatalogPageSupport.addModelAttributes(model, result, pageable, "/employees",
                CatalogPageSupport.sortOptions("id", "ID", "firstName", "Имя",
                        "lastName", "Фамилия", "email", "Электронная почта",
                        "role", "Роль", "active", "Статус",
                        "registrationDate", "Дата регистрации"));
        return "employees/employees";
    }

    /** Открывает форму создания учётной записи сотрудника. */
    @GetMapping("/employees/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String registration(Model model) {

        model.addAttribute(
                "employee",
                new CreateEmployeeDTO()
        );

        model.addAttribute(
                "roles",
                roles
        );

        return "employees/create_employee";
    }

    /** Сохраняет новую или изменённую учётную запись сотрудника. */
    @PostMapping("/employees/save")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String save(
            @ModelAttribute("employee")
            @Valid
            CreateEmployeeDTO createEmployeeDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        model.addAttribute(
                "employee",
                createEmployeeDTO
        );

        model.addAttribute(
                "roles",
                roles
        );

        if (employeeService.existsByEmail(
                createEmployeeDTO.getEmail()
        )) {

            bindingResult.addError(
                    new FieldError(
                            "employee",
                            "email",
                            "Пользователь с таким email уже существует"
                    )
            );
        }

        validatePasswordConfirmation(
                createEmployeeDTO,
                bindingResult
        );

        if (bindingResult.hasErrors()) {
            return "employees/create_employee";
        }

        try {

            Employee employee = authService.createEmployee(
                    EmployeeDtoMapper.toDomain(createEmployeeDTO),
                    createEmployeeDTO.getPassword(),
                    createEmployeeDTO.getConfirmPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Пользователь успешно создан"
            );

            return "redirect:/employees/" + employee.getId();

        } catch (IllegalArgumentException exception) {
            model.addAttribute(
                    "errorMessage",
                    "Не удалось создать сотрудника: " + exception.getMessage()
            );
            return "employees/create_employee";
        }
    }

    /** Деактивирует учётную запись выбранного сотрудника. */
    @PostMapping("/employees/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String deactivateEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        employeeService.deactivateById(id);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Учётная запись деактивирована"
        );
        return "redirect:/employees/" + id;
    }

    /** Активирует учётную запись выбранного сотрудника. */
    @PostMapping("/employees/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String activateEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        employeeService.activateById(id);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Учётная запись активирована"
        );
        return "redirect:/employees/" + id;
    }

    /** Открывает форму редактирования учётной записи сотрудника. */
    @GetMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editEmployee(
            @PathVariable int id,
            Model model
    ) {

        EmployeeDTO user = EmployeeDtoMapper.toDto(employeeService.loadUserById(id));

        model.addAttribute(
                "employee",
                user
        );

        model.addAttribute(
                "roles",
                roles
        );

        model.addAttribute(
                "id",
                user.getId()
        );
        model.addAttribute(
                "active",
                employeeService.isActive(user.getId())
        );

        return "employees/employee";
    }

    /** Сохраняет изменения учётной записи сотрудника. */
    @PostMapping("/employees/{id}/update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String update(
            @PathVariable int id,
            @ModelAttribute("employee")
            @Valid
            ProfileDTO employeeUpd,
            BindingResult bindingResult,
            Model model
    ) {

        model.addAttribute(
                "roles",
                roles
        );

        model.addAttribute(
                "employee",
                employeeUpd
        );

        model.addAttribute(
                "id",
                employeeUpd.getId()
        );
        model.addAttribute(
                "active",
                employeeService.isActive((long) id)
        );

        if (bindingResult.hasErrors()) {
            return "employees/employee";
        }

        Employee employee = employeeService.loadUserById(id);

        if (!employee.getEmail().equals(
                employeeUpd.getEmail()
        )
                && employeeService.existsByEmail(
                employeeUpd.getEmail()
        )) {

            bindingResult.addError(
                    new FieldError(
                            "employee",
                            "email",
                            "Пользователь с таким email уже существует"
                    )
            );

            return "employees/employee";
        }

        employee.changePersonalData(
                employeeUpd.getFirstName(),
                employeeUpd.getLastName(),
                employeeUpd.getEmail()
        );
        employee.changeRole(employeeUpd.getRole());

        Employee saved = employeeService.save(employee);

        return "redirect:/employees/" + saved.getId();
    }

    private void validatePasswordConfirmation(
            ChangePasswordDTO dto,
            BindingResult bindingResult
    ) {

        if (!dto.getPassword().equals(
                dto.getConfirmPassword()
        )) {

            bindingResult.addError(
                    new FieldError(
                            "changePassword",
                            "confirmPassword",
                            "Пароли не совпадают"
                    )
            );
        }
    }

    private void validatePasswordConfirmation(
            CreateEmployeeDTO dto,
            BindingResult bindingResult
    ) {

        if (!dto.getPassword().equals(
                dto.getConfirmPassword()
        )) {

            bindingResult.addError(
                    new FieldError(
                            "employee",
                            "confirmPassword",
                            "Пароли не совпадают"
                    )
            );
        }
    }
}