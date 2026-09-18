package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.ProjectService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
public class ProjectController
        extends BaseCatalogController {

    private final ProjectService projectService;

    public ProjectController(
            ProjectService projectService
    ) {
        this.projectService =
                projectService;
    }

    @GetMapping("/create")
    public String createProject(
            Model model
    ) {
        model.addAttribute(
                "project",
                projectService.createProject()
        );

        return "projects/project";
    }

    @GetMapping("/{id}")
    public String editProject(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute(
                "project",
                projectService.findById(id)
        );

        return "projects/project";
    }

    @PostMapping("/{id}/save")
    public String saveProject(
            @ModelAttribute("project")
            ProjectDTO project,
            Model model
    ) {
        try {
            ProjectDTO saved =
                    projectService.saveProject(
                            project
                    );

            return "redirect:/projects/"
                    + saved.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "projectErrors",
                    e.getErrors()
            );

            model.addAttribute(
                    "project",
                    project
            );

            return "projects/project";
        }
    }

    @PostMapping("/{id}/addALS")
    public String addALS(
            @PathVariable Long id
    ) {
        ProjectDTO project =
                projectService
                        .addNewALSandSaveProject(id);

        return "redirect:/projects/"
                + project.getId();
    }

    @GetMapping("/{projectId}/alss/{alsId}/delete")
    public String deleteALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {
        ProjectDTO project =
                projectService
                        .deleteALSandSaveProject(
                                projectId,
                                alsId
                        );

        return "redirect:/projects/"
                + project.getId();
    }

    @GetMapping("/{projectId}/alss/{alsId}")
    public String editALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            Model model
    ) {
        ProjectDTO project =
                projectService.findById(
                        projectId
                );

        ALSDTO als =
                project.getAlsList()
                        .stream()
                        .filter(
                                value ->
                                        value.getId() != null
                                                && value.getId().equals(alsId)
                        )
                        .findFirst()
                        .orElseThrow();

        model.addAttribute(
                "project",
                project
        );

        model.addAttribute(
                "als",
                als
        );

        return "projects/project_als";
    }

    @PostMapping("/{projectId}/alss/{alsId}/save")
    public String saveALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @ModelAttribute("als") ALSDTO als,
            Model model
    ) {
        try {

            ProjectDTO project =
                    projectService.findById(
                            projectId
                    );

            projectService.replaceALSandSaveProject(
                    project,
                    als,
                    alsId
            );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + als.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "ALSErrors",
                    e.getErrors()
            );

            model.addAttribute(
                    "als",
                    als
            );

            return "projects/project_als";
        }
    }

    @PostMapping("/{projectId}/alss/{alsId}/addLB")
    public String addLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {
        ALSDTO als =
                projectService.addLBAtProject(
                        projectId,
                        alsId
                );

        return "redirect:/projects/"
                + projectId
                + "/alss/"
                + als.getId();
    }

    @GetMapping("/{id}/savetoexcel")
    public ResponseEntity<Resource> saveProjectToExcel(
            @PathVariable Long id
    ) {
        ProjectDTO project =
                projectService.findById(id);

        String filename =
                project.getName()
                        + ".xlsx";

        InputStreamResource file =
                new InputStreamResource(
                        projectService.exportToExcel(
                                project
                        )
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(file);
    }
}