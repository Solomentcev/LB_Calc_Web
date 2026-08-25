package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ChangePasswordDTO;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.model.user.Role;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmployeeController.class);
    private final List<Role> roles;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeService employeeService, PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.passwordEncoder = passwordEncoder;
        roles= Arrays.asList(Role.values());
    }
    @GetMapping("/myprofile")
    public String myProfile(Model model) {
        logger.info("=== MYPROFILE CONTROLLER START ===");

        EmployeeDTO employee= employeeService.getCurrentEmployee();

        logger.info("EmployeeDTO = {}", employee);
        logger.info("EmployeeDTO id = {}", employee.getId());

        model.addAttribute("employee",employee);

        logger.info("Model employee = {}", model.getAttribute("employee"));

        return "myprofile";
    }
    @GetMapping("/myprofile/changepassword")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePassword", new ChangePasswordDTO());
        model.addAttribute("action","myprofile/changepassword");
        return "change_password";
    }
    @PostMapping("/myprofile/changepassword")
    public String changePassword(@ModelAttribute("changePassword") @Valid ChangePasswordDTO changePasswordDTO,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                 Model model) {
        model.addAttribute("changePassword", changePasswordDTO);
        model.addAttribute("action","myprofile/changepassword");
        if (!changePasswordDTO.getPassword().equals(changePasswordDTO.getConfirmPassword())) {
            bindingResult.addError(new FieldError(
                    "changePassword",
                    "confirmPassword",
                    "пароли не совпадают"
            ));
        }
        if (bindingResult.hasErrors()) {
            return "change_password";
        }
        try {
            EmployeeDTO employee= employeeService.getCurrentEmployee();
            employee.setPassword(changePasswordDTO.getPassword());
            employee.setEncryptedPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
            employee=employeeService.save(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно заменен");
            return "redirect:/myprofile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка смены пароля: " + e.getMessage());
            return "change_password";
        }
    }
    @GetMapping("/employees/{id}/changepassword")
    public String changeEmployeePasswordForm(@PathVariable(value = "id") int id,
                                            Model model) {
        model.addAttribute("changePassword", new ChangePasswordDTO());
        model.addAttribute("id", id);
        model.addAttribute("action","/employees/{id}/changepassword (id=${id})");
        return "change_passwordEmployee";
    }
    @PostMapping("/employees/{id}/changepassword")
    public String changeEmployeePassword(@PathVariable(value = "id") int id,
                                @ModelAttribute("changePassword") @Valid ChangePasswordDTO changePasswordDTO,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                 Model model) {
        model.addAttribute("changePassword", changePasswordDTO);
        if (!changePasswordDTO.getPassword().equals(changePasswordDTO.getConfirmPassword())) {
            bindingResult.addError(new FieldError(
                    "changePassword",
                    "confirmPassword",
                    "пароли не совпадают"
            ));
        }
        if (bindingResult.hasErrors()) {
            return "change_passwordEmployee";
        }
        try {
            EmployeeDTO employee= employeeService.loadUserById(id);
            employee.setPassword(changePasswordDTO.getPassword());
            employee.setEncryptedPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
            employee=employeeService.save(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно заменен");
            return "redirect:/employees/"+employee.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка смены пароля: " + e.getMessage());
            return "change_password";
        }
    }
    @GetMapping("/profiles")
    public String profiles(Model model) {
        model.addAttribute("employees", employeeService.getAllProfiles());
        return "profiles/profiles";
    }
    @GetMapping("/profiles/{id}")
    public String getProfile(@PathVariable(value = "id") int id, Model model) {
        ProfileDTO user= employeeService.getProfileById(id);
        model.addAttribute("employee",user);
        model.addAttribute("roles",roles);
        model.addAttribute("id",user.getId());
        return "profiles/profile";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeService.getAllProfiles());
        return "employees/employees";
    }

    @GetMapping("/employees/create")
    public String registration(Model model) {
        model.addAttribute("employee", new CreateEmployeeDTO());
        model.addAttribute("roles", roles);
        return "employees/create_employee";
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/employees/save")
    public String save(@ModelAttribute("employee") @Valid CreateEmployeeDTO createEmployeeDTO,
                       BindingResult bindingResult, RedirectAttributes redirectAttributes,
                       Model model) {
          model.addAttribute("employee", createEmployeeDTO);
          model.addAttribute("roles",roles);
        if (employeeService.existsByEmail(createEmployeeDTO.getEmail())) {
            bindingResult.addError(new FieldError(
                    "employee",
                    "email",
                    "Пользователь с таким email уже существует"
            ));
        }
        if (!createEmployeeDTO.getPassword().equals(createEmployeeDTO.getConfirmPassword())) {
            bindingResult.addError(new FieldError(
                    "employee",
                    "confirmPassword",
                    "Пароли не совпадают"
            ));
        }
        if (bindingResult.hasErrors()) {
            return "employees/create_employee";
        }
        try {
            EmployeeDTO employeeDTO=new EmployeeDTO();
            employeeDTO.setFirstName(createEmployeeDTO.getFirstName());
            employeeDTO.setLastName(createEmployeeDTO.getLastName());
            employeeDTO.setEmail(createEmployeeDTO.getEmail());
            employeeDTO.setRole(createEmployeeDTO.getRole());
            employeeDTO.setPassword(createEmployeeDTO.getPassword());
            employeeDTO.setEncryptedPassword(passwordEncoder.encode(createEmployeeDTO.getPassword()));
            employeeDTO=employeeService.save(employeeDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно создан");
            return "redirect:/employees/"+ employeeDTO.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка регистрации: " + e.getMessage());
            return "employees/create_employee";
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/employees/{id}")
    public String editEmployee(@PathVariable(value = "id") int id, Model model) {
        EmployeeDTO user= employeeService.loadUserById(id);
        model.addAttribute("employee",user);
        model.addAttribute("roles",roles);
        model.addAttribute("id",user.getId());
        return "employees/employee";
    }

    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/employees/{id}/update")
    public String update(@PathVariable(value = "id") int id,
                          @ModelAttribute("employee")  @Valid ProfileDTO employeeUpd,
                          BindingResult bindingResult,
                          Model model) {
        model.addAttribute("roles",roles);
        model.addAttribute("employee",employeeUpd);
        model.addAttribute("id",employeeUpd.getId());
        if (bindingResult.hasErrors()) {
            return "employees/employee";
        }
        EmployeeDTO employeeDTO=employeeService.loadUserById(id);
        if (!employeeDTO.getEmail().equals(employeeUpd.getEmail())
                && employeeService.existsByEmail(employeeUpd.getEmail())) {
                bindingResult.addError(new FieldError(
                        "employee",
                        "email",
                        "Пользователь с таким email уже существует"
                ));
            return "employees/employee";
            }
        employeeDTO.setFirstName(employeeUpd.getFirstName());
        employeeDTO.setLastName(employeeUpd.getLastName());
        employeeDTO.setEmail(employeeUpd.getEmail());
        employeeDTO.setRole(employeeUpd.getRole());
        employeeDTO=employeeService.save(employeeDTO);

        return "redirect:/employees/"+employeeDTO.getId();
    }
}
