package com.lb_calc_web.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller

public class EmployeeController {
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
        EmployeeDTO employee= employeeService.getCurrentEmployee();
        model.addAttribute("employee",employee);
        return "myprofile";
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeService.findAll());
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
                    "пароли не совпадают"
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
    public String edit(@PathVariable(value = "id") int id, Model model) {
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
            System.out.println("error");
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
