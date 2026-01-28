package com.lb_calc_web.controller;

import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.ProjectService;
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

    public LBCalcWebController(ProjectService projectService, ALSService alsService, LCService lcService, LBService lbService) {
        this.projectService = projectService;
        this.alsService = alsService;
        this.lcService = lcService;
        this.lbService = lbService;
    }
    @GetMapping
    private String init() {return "index";}

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
}
