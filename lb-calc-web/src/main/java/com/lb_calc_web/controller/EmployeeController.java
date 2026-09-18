package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ChangePasswordDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

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

    @GetMapping("/myprofile")
    public String myProfile(Model model) {

        EmployeeDTO employee =
                employeeService.getCurrentEmployee();

        model.addAttribute(
                "employee",
                employee
        );

        return "myprofile";
    }

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

            EmployeeDTO employee =
                    employeeService.getCurrentEmployee();

            authService.changePassword(
                    employee.getId(),
                    changePasswordDTO.getPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Пароль успешно заменен"
            );

            return "redirect:/myprofile";

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Ошибка смены пароля: " +
                            e.getMessage()
            );

            return "change_password";
        }
    }

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

            EmployeeDTO employee =
                    employeeService.loadUserById(id);

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

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Ошибка смены пароля: " +
                            e.getMessage()
            );

            return "change_passwordEmployee";
        }
    }

    @GetMapping("/profiles")
    public String profiles(Model model) {

        model.addAttribute(
                "employees",
                employeeService.getAllProfiles()
        );

        return "profiles/profiles";
    }

    @GetMapping("/profiles/{id}")
    public String getProfile(
            @PathVariable int id,
            Model model
    ) {

        ProfileDTO user =
                employeeService.getProfileById(id);

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

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String employees(Model model) {

        model.addAttribute(
                "employees",
                employeeService.getAllProfiles()
        );

        return "employees/employees";
    }

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

            EmployeeDTO employeeDTO =
                    authService.createEmployee(
                            createEmployeeDTO
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Пользователь успешно создан"
            );

            return "redirect:/employees/" +
                    employeeDTO.getId();

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Ошибка создания сотрудника: " +
                            e.getMessage()
            );

            return "employees/create_employee";
        }
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editEmployee(
            @PathVariable int id,
            Model model
    ) {

        EmployeeDTO user =
                employeeService.loadUserById(id);

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

        return "employees/employee";
    }

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

        if (bindingResult.hasErrors()) {
            return "employees/employee";
        }

        EmployeeDTO employee =
                employeeService.loadUserById(id);

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

        return "redirect:/employees/" +
                saved.getId();
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