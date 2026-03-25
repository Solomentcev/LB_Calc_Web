package com.lb_calc_web.controller;

import com.lb_calc_web.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class LBCalcWebController {
    private final ProjectService projectService;
    private final ALSService alsService;
    private final LCService lcService;
    private final LBService lbService;
    private final EmployeeService employeeService;

    public LBCalcWebController(ProjectService projectService, ALSService alsService, LCService lcService, LBService lbService, EmployeeService employeeService) {
        this.projectService = projectService;
        this.alsService = alsService;
        this.lcService = lcService;
        this.lbService = lbService;
        this.employeeService = employeeService;
    }
    @GetMapping
    private String init() {return "/profile";}

    @GetMapping("/projects")
    private String projects(Model model) {
        model.addAttribute("projects",projectService.findAll());
        return "projects/projects";
    }
    @GetMapping("/alss")
    private String alss(Model model) {
        model.addAttribute("alss", alsService.findAll());
        return "alss/alss";
    }
    @GetMapping("/lcs")
    private String lcs(Model model) {
        model.addAttribute("lcs", lcService.findAll());
        return "lcs/lcs";
    }
    @GetMapping("/lbs")
    private String lbs(Model model) {

        model.addAttribute("lbs", lbService.findAll());
        return "lbs/lbs";
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employees/employees";
    }

}
