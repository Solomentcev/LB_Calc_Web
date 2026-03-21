package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.ProjectController;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
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

    @PostMapping("/{id}/update")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO project) {
        logger.debug("Updating project {}", project);
        if (project.getId() == 0) {
            return ResponseEntity.badRequest().build();
        }

        if (project.getId() != id.intValue()) {
            return ResponseEntity.badRequest().build();
        }
        for(ALSDTO als:project.getAlsList()){
            als=alsService.resizeLC(als);
            als=alsService.resizeLBs(als);
        }
        List<List<List<List<String>>>>  errorProjectList= SizeValidator.getErrorValidateProjectSizeList(project);
        if (errorProjectList.isEmpty()) {
            project=projectService.saveProject(project);
        }else{
            return ResponseEntity.badRequest().build();
        }

        return new ResponseEntity<>(project, HttpStatus.OK);
    }
    @PostMapping("/save")
    public ResponseEntity<ProjectDTO> saveNewProject(@RequestBody ProjectDTO project) {
        logger.debug("Saving new project {}", project);
        for(ALSDTO als:project.getAlsList()){
            als=alsService.resizeLC(als);
            als=alsService.resizeLBs(als);
        }
        List<List<List<List<String>>>>  errorProjectList= SizeValidator.getErrorValidateProjectSizeList(project);
        if (errorProjectList.isEmpty()) {
            project=projectService.saveProject(project);
        }else{
            return ResponseEntity.badRequest().build();
        }

        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    private ResponseEntity<ProjectDTO> editProject(@PathVariable Long id) {
        ProjectDTO project =projectService.findById(id);

        return new ResponseEntity<>(project, HttpStatus.OK);
    }
}
