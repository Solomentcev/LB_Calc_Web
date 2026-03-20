package com.lb_calc_web.controller;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.model.user.Role;
import com.lb_calc_web.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller

public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final List<Role> roles;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeService employeeService, PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.passwordEncoder = passwordEncoder;
        roles= Arrays.asList(Role.values());
    }

    @PostMapping("/employees/save")
    private String save(@ModelAttribute("employee") @Valid EmployeeDTO employeeDTO,
                        BindingResult bindingResult,
                        Model model) {
        model.addAttribute("roles",roles);
        employeeDTO.setEncryptedPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        if (employeeService.existsByEmail(employeeDTO.getEmail())) {
                bindingResult.addError(new FieldError(
                        "employee",
                        "email",
                        "Пользователь с таким email уже существует"
                ));
            }
        if (bindingResult.hasErrors()) {
            return "employees/employee";
        }
        employeeDTO= employeeService.save(employeeDTO);
        model.addAttribute("employee",employeeDTO);

        model.addAttribute("id",employeeDTO.getId());
        return "redirect:/employees/"+employeeDTO.getId();
    }
    @PostMapping("/employees/{id}/update")
    private String update(@PathVariable(value = "id") int id,
                          @ModelAttribute("employee")  @Valid EmployeeDTO employeeUpd,
                          BindingResult bindingResult,
                          Model model) {
        model.addAttribute("roles",roles);
        System.out.println("uuupppddddd");
        EmployeeDTO employeeDTO=employeeService.loadUserById(id);
        if (!employeeDTO.getEmail().equals(employeeUpd.getEmail())) {
            if (employeeService.existsByEmail(employeeUpd.getEmail())) {
                bindingResult.addError(new FieldError(
                        "employee",
                        "email",
                        "Пользователь с таким email уже существует"
                ));

            }
        }
        if (bindingResult.hasErrors()) {
          //  System.out.println(bindingResult);
            return "employees/employee";
        }
        employeeDTO.setRole(employeeUpd.getRole());
        employeeDTO.setFirstName(employeeUpd.getFirstName());
        employeeDTO.setLastName(employeeUpd.getLastName());
        employeeDTO.setEmail(employeeUpd.getEmail());
        employeeUpd= employeeService.save(employeeDTO);
        model.addAttribute("employee",employeeUpd);
        model.addAttribute("id",employeeUpd.getId());
        return "redirect:/employees/"+employeeUpd.getId();
    }
    @GetMapping("/employees/{id}")
    private String edit(@PathVariable(value = "id") int id, Model model) {
        EmployeeDTO user= employeeService.loadUserById(id);
        model.addAttribute("employee",user);
        model.addAttribute("roles",roles);
        model.addAttribute("id",user.getId());
        return "employees/employee";
    }
    @GetMapping("/profile")
    private String profile(Model model) {
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        EmployeeDTO employee= (EmployeeDTO) auth.getPrincipal();
        model.addAttribute("employee",employee);
        return "profile";
    }
}
