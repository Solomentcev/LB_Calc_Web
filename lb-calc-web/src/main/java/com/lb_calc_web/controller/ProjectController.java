package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.image.ALSImageService;
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
    private final ALSService alsService;

    public ProjectController(
            ProjectService projectService,
            ALSService alsService
    ) {
        this.projectService = projectService;
        this.alsService = alsService;
    }

    /**
     * Список всех проектов.
     */
    @GetMapping
    public String projects(
            Model model
    ) {
        model.addAttribute(
                "projects",
                ProjectDtoMapper.toDtoList(projectService.findAll())
        );

        return "projects/projects";
    }

    /**
     * Форма создания проекта.
     */
    @GetMapping("/create")
    public String createProject(
            Model model
    ) {
        model.addAttribute(
                "project",
                addALSImages(projectService.createProject())
        );

        return "projects/project";
    }

    /**
     * Форма редактирования проекта.
     */
    @GetMapping("/{id}")
    public String editProject(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute(
                "project",
                addALSImages(ProjectDtoMapper.toDto(projectService.findById(id)))
        );

        return "projects/project";
    }

    /**
     * Сохранение проекта.
     */
    @PostMapping("/{id}/save")
    public String saveProject(
            @PathVariable Long id,
            @ModelAttribute("project") ProjectDTO project,
            Model model
    ) {
        try {
            /*
             * ID из URL имеет приоритет над значением формы.
             */
            project.setId(id);

            com.lb_calc_web.domain.model.Project domain =
                    com.lb_calc_web.mapper.dto.ProjectDtoMapper.toDomain(
                            project,
                            alsService::restoreDomain
                    );
            com.lb_calc_web.domain.model.Project saved =
                    projectService.saveProject(domain);

            return "redirect:/projects/"
                    + saved.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "projectErrors",
                    e.getErrors()
            );

            model.addAttribute("project", addALSImages(project));

            return "projects/project";
        }
    }

    /**
     * Добавление ALS в проект.
     */
    @PostMapping("/{id}/addALS")
    public String addALS(
            @PathVariable Long id,
            @ModelAttribute("project") ProjectDTO draft,
            Model model
    ) {
        /*
         * Проект с ID 0 ещё не сохранён. В этом сценарии добавляем
         * ALS в текущую форму и не пытаемся загружать проект из БД.
         */
        if (id == null || id <= 0) {
            draft.getAlsList().add(ALSDtoMapper.toDto(alsService.createALS()));
            model.addAttribute("project", addALSImages(draft));
            return "projects/project";
        }

        ProjectDTO project =
                projectService.addNewALSandSaveProject(id);

        return "redirect:/projects/"
                + project.getId();
    }

    /**
     * Удаление ALS из проекта.
     */
    @PostMapping("/{projectId}/alss/{alsId}/delete")
    public String deleteALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {
        ProjectDTO project =
                projectService.deleteALSandSaveProject(
                        projectId,
                        alsId
                );

        return "redirect:/projects/"
                + project.getId();
    }

    /**
     * Форма редактирования ALS.
     */
    @GetMapping("/{projectId}/alss/{alsId}")
    public String editALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            Model model
    ) {
        ProjectDTO project =
                ProjectDtoMapper.toDto(projectService.findById(projectId));

        ALSDTO als =
                ALSDtoMapper.toDto(
                        projectService.findALSInProject(projectId, alsId)
                );

        model.addAttribute(
                "project",
                addALSImages(project)
        );

        model.addAttribute(
                "als",
                addALSImage(als)
        );

        return "projects/project_als";
    }

    /**
     * Сохранение ALS.
     */
    @PostMapping("/{projectId}/alss/{alsId}/save")
    public String saveALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @ModelAttribute("als") ALSDTO als,
            Model model
    ) {
        try {
            ALSDTO savedALS =
                    projectService.replaceALSandSaveProject(
                            projectId,
                            alsId,
                            als
                    );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "ALSErrors",
                    e.getErrors()
            );

            model.addAttribute("als", addALSImage(als));

            model.addAttribute(
                    "project",
                    addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId)))
            );

            return "projects/project_als";
        }
    }

    /**
     * Форма редактирования LC.
     */
    @GetMapping(
            "/{projectId}/alss/{alsId}/lcs/{lcId}"
    )
    public String editLC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lcId,
            Model model
    ) {
        ProjectDTO project =
                ProjectDtoMapper.toDto(projectService.findById(projectId));

        ALSDTO als =
                ALSDtoMapper.toDto(
                        projectService.findALSInProject(projectId, alsId)
                );

        LCDTO lc =
                LCDtoMapper.toDto(
                        projectService.findLCInProject(projectId, alsId, lcId)
                );

        model.addAttribute("project", addALSImages(project));
        model.addAttribute("als", addALSImage(als));
        model.addAttribute("lc", lc);
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lcId", lcId);

        return "projects/project_lc";
    }

    /**
     * Сохранение LC.
     */
    @PostMapping(
            "/{projectId}/alss/{alsId}/lcs/{lcId}/save"
    )
    public String saveLC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lcId,
            @ModelAttribute("lc") LCDTO lc,
            Model model
    ) {
        try {
            lc.setId(lcId);

            ALSDTO savedALS =
                    projectService.saveLCAtProject(
                            projectId,
                            alsId,
                            lcId,
                            lc
                    );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "errors",
                    e.getErrors()
            );

            model.addAttribute(
                    "project",
                    addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId)))
            );

            model.addAttribute(
                    "als",
                    addALSImage(ALSDtoMapper.toDto(
                            projectService.findALSInProject(projectId, alsId)
                    ))
            );

            model.addAttribute(
                    "lc",
                    lc
            );

            model.addAttribute(
                    "projectId",
                    projectId
            );

            model.addAttribute(
                    "alsId",
                    alsId
            );

            model.addAttribute(
                    "lcId",
                    lcId
            );

            return "projects/project_lc";
        }
    }

    /**
     * Форма редактирования LBC.
     */
    @GetMapping(
            "/{projectId}/alss/{alsId}/lbcs/{lbcId}"
    )
    public String editLBC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            Model model
    ) {
        ProjectDTO project =
                ProjectDtoMapper.toDto(projectService.findById(projectId));

        ALSDTO als =
                ALSDtoMapper.toDto(
                        projectService.findALSInProject(projectId, alsId)
                );

        LBCDTO lbc =
                LBCDtoMapper.toDto(
                        projectService.findLBCInProject(projectId, alsId, lbcId)
                );

        model.addAttribute("project", addALSImages(project));
        model.addAttribute("als", addALSImage(als));
        model.addAttribute("lbc", lbc);
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lbcId", lbcId);

        return "projects/project_lbc";
    }

    /**
     * Сохранение LBC в ALS проекта.
     */
    @PostMapping(
            "/{projectId}/alss/{alsId}/lbcs/{lbcId}/save"
    )
    public String saveLBC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            @ModelAttribute("lbc") LBCDTO lbc,
            Model model
    ) {
        try {
            lbc.setId(lbcId);

            ALSDTO savedALS =
                    projectService.saveLBCAtProject(
                            projectId,
                            alsId,
                            lbcId,
                            lbc
                    );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {
            model.addAttribute(
                    "errors",
                    e.getErrors()
            );

            model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId))));

            model.addAttribute("als", addALSImage(ALSDtoMapper.toDto(
                    projectService.findALSInProject(projectId, alsId)
            )));

            model.addAttribute(
                    "lbc",
                    lbc
            );

            model.addAttribute("projectId", projectId);
            model.addAttribute("alsId", alsId);
            model.addAttribute("lbcId", lbcId);

            return "projects/project_lbc";
        }
    }

    /**
     * Форма редактирования LB.
     */
    @GetMapping(
            "/{projectId}/alss/{alsId}/lbs/{lbId}"
    )
    public String editLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId,
            Model model
    ) {
        ProjectDTO project =
                ProjectDtoMapper.toDto(projectService.findById(projectId));

        ALSDTO als =
                ALSDtoMapper.toDto(
                        projectService.findALSInProject(projectId, alsId)
                );

        LBDTO lb =
                LBDtoMapper.toDto(
                        projectService.findLBInProject(projectId, alsId, lbId)
                );

        model.addAttribute("project", addALSImages(project));
        model.addAttribute("als", addALSImage(als));
        model.addAttribute("lb", lb);
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lbId", lbId);

        return "projects/project_lb";
    }

    /**
     * Сохранение LB.
     */
    @PostMapping(
            "/{projectId}/alss/{alsId}/lbs/{lbId}/save"
    )
    public String saveLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId,
            @ModelAttribute("lb") LBDTO lb,
            Model model
    ) {
        try {
            lb.setId(lbId);

            ALSDTO savedALS =
                    projectService.saveLBAtProject(
                            projectId,
                            alsId,
                            lbId,
                            lb
                    );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "errors",
                    e.getErrors()
            );

            model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId))));

            model.addAttribute("als", addALSImage(ALSDtoMapper.toDto(projectService.findALSInProject(projectId, alsId))));

            model.addAttribute(
                    "lb",
                    lb
            );

            model.addAttribute(
                    "projectId",
                    projectId
            );

            model.addAttribute(
                    "alsId",
                    alsId
            );

            model.addAttribute(
                    "lbId",
                    lbId
            );

            return "projects/project_lb";
        }
    }

    /**
     * Заменяет control-модуль ALS проекта на новый LBC.
     */
    @PostMapping(
            "/{projectId}/alss/{alsId}/replaceLBC"
    )
    public String replaceLBC(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {
        ALSDTO als =
                projectService.replaceWithNewLBCAtProject(
                        projectId,
                        alsId
                );

        return "redirect:/projects/"
                + projectId
                + "/alss/"
                + als.getId();
    }

    /**
     * Добавление LB в ALS.
     */
    @PostMapping(
            "/{projectId}/alss/{alsId}/addLB"
    )
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

    /**
     * Удаление LB из ALS проекта.
     */
    @PostMapping(
            "/{projectId}/alss/{alsId}/lbs/{lbId}/delete"
    )
    public String deleteLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {
        ALSDTO als =
                projectService.deleteLBatProject(
                        projectId,
                        alsId,
                        lbId
                );

        return "redirect:/projects/"
                + projectId
                + "/alss/"
                + als.getId();
    }

    /**
     * Экспорт проекта в Excel.
     */
    @GetMapping("/{id}/savetoexcel")
    public ResponseEntity<Resource> saveProjectToExcel(
            @PathVariable Long id
    ) {
        ProjectDTO project =
                ProjectDtoMapper.toDto(projectService.findById(id));

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
    /**
     * Добавляет изображения ALS перед передачей проекта в представление.
     */
    private ProjectDTO addALSImages(ProjectDTO project) {
        if (project != null && project.getAlsList() != null) {
            project.getAlsList().forEach(this::addALSImage);
        }
        return project;
    }

    /**
     * Формирует изображение конфигурации для представления.
     */
    private ALSDTO addALSImage(ALSDTO als) {
        if (als != null) {
            als.setStringALSImage(ALSImageService.getStringALSImage(als));
        }
        return als;
    }

}