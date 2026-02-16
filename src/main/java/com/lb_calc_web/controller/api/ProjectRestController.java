package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.ProjectController;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final ALSService alsService;
    private final LBService lbService;
    private final LCService lcService;

    public ProjectRestController(ProjectService projectService, ALSService alsService, LBService lbService, LCService lcService) {
        this.projectService = projectService;
        this.alsService = alsService;
        this.lbService = lbService;
        this.lcService = lcService;
    }
    @GetMapping("/create")
    private ResponseEntity<ProjectDTO> createProject() {
        ProjectDTO project = projectService.createProject();

        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    private ResponseEntity<ProjectDTO> editProject(@PathVariable Long id) {
        ProjectDTO project =projectService.findById(id);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }
}
