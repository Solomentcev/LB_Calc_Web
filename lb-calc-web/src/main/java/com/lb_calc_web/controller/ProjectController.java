package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.export.ProjectExcelExportService;
import com.lb_calc_web.service.image.ALSImageService;
import com.lb_calc_web.service.image.LBImageService;
import com.lb_calc_web.service.image.LCImageService;
import com.lb_calc_web.service.image.LBCImageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController
        extends BaseCatalogController {

    @ModelAttribute("pageContext")
    public String pageContext() {
        return "PROJECT";
    }

    private final ProjectService projectService;
    private final ALSService alsService;
    private final ProjectExcelExportService projectExcelExportService;
    private final EquipmentCatalogService equipmentCatalogService;

    public ProjectController(
            ProjectService projectService,
            ALSService alsService,
            ProjectExcelExportService projectExcelExportService,
            EquipmentCatalogService equipmentCatalogService
    ) {
        this.projectService = projectService;
        this.alsService = alsService;
        this.projectExcelExportService = projectExcelExportService;
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /**
     * Список всех проектов.
     */
    @GetMapping
    public String projects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "company", "createdAt", "updatedAt"));
        var result = projectService.findPage(q, pageable).map(ProjectDtoMapper::toDto);
        model.addAttribute("projects", result.getContent());
        CatalogPageSupport.addModelAttributes(model, result, q, pageable, "/projects",
                CatalogPageSupport.sortOptions("id", "ID", "name", "Название",
                        "company", "Компания", "createdAt", "Дата создания",
                        "updatedAt", "Дата изменения"));
        return "projects/projects";
    }

    /**
     * Форма создания проекта.
     */
    @GetMapping("/create")
    public String createProject(
            Model model
    ) {
        ProjectDTO project = ProjectDtoMapper.toDto(projectService.createProject());
        project.setId(0L);
        model.addAttribute("project", addALSImages(project));

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
            populateServerMetadata(project, id);

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
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute("project", addALSImages(project));

            return "projects/project";
        }
    }

    /**
     * Восстанавливает серверные метаданные проекта перед преобразованием DTO в Domain.
     *
     * <p>HTML-форма не является источником данных о создателе и датах проекта.
     * Для нового проекта метаданные формируются для текущего сотрудника,
     * для существующего — берутся из сохранённого проекта.</p>
     *
     * @param dto DTO проекта из формы
     * @param projectId идентификатор проекта из URL
     */
    private void populateServerMetadata(ProjectDTO dto, Long projectId) {
        com.lb_calc_web.domain.model.Project metadata =
                projectId != null && projectId > 0
                        ? projectService.findById(projectId)
                        : projectService.initProject(dto.getCompany());

        ProjectDTO metadataDto = ProjectDtoMapper.toDto(metadata);
        dto.setCreatedBy(metadataDto.getCreatedBy());
        dto.setCreatedAt(metadataDto.getCreatedAt());
        dto.setUpdatedBy(metadataDto.getUpdatedBy());
        dto.setUpdatedAt(metadataDto.getUpdatedAt());
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

        com.lb_calc_web.domain.model.Project project =
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
        com.lb_calc_web.domain.model.Project project =
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
                ALSImageService.addImages(als)
        );

        return "alss/als";
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
            projectService.findALSInProject(projectId, alsId);
            als.setId(alsId);
            ALS replacement = alsService.restoreDomain(als);
            ALS savedALS = projectService.replaceALSandSaveProject(
                    projectId,
                    alsId,
                    replacement
            );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "ALSErrors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute("als", ALSImageService.addImages(als));

            model.addAttribute(
                    "project",
                    addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId)))
            );

            return "alss/als";
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
        model.addAttribute("als", ALSImageService.addImages(als));
        prepareLCForm(model, LCImageService.addImage(lc));
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lcId", lcId);

        return "lcs/lc";
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
            projectService.findLCInProject(projectId, alsId, lcId);
            ALS sourceALS = projectService.findALSInProject(projectId, alsId);
            LC replacement = alsService.restoreLC(lc, sourceALS);
            ALS savedALS = projectService.saveLCAtProject(
                    projectId,
                    alsId,
                    lcId,
                    replacement
            );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "project",
                    addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId)))
            );

            model.addAttribute(
                    "als",
                    ALSImageService.addImages(ALSDtoMapper.toDto(
                            projectService.findALSInProject(projectId, alsId)
                    ))
            );

            prepareLCForm(model, LCImageService.addImage(lc));

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

            return "lcs/lc";
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
        model.addAttribute("als", ALSImageService.addImages(als));
        prepareLBCForm(model, LBCImageService.addImage(lbc));
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lbcId", lbcId);

        return "lbcs/lbc";
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
            projectService.findLBCInProject(projectId, alsId, lbcId);
            ALS sourceALS = projectService.findALSInProject(projectId, alsId);
            LBC replacement = alsService.restoreLBC(lbc, sourceALS);
            ALS savedALS = projectService.saveLBCAtProject(
                    projectId,
                    alsId,
                    lbcId,
                    replacement
            );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {
            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId))));

            model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(
                    projectService.findALSInProject(projectId, alsId)
            )));

            prepareLBCForm(model, LBCImageService.addImage(lbc));

            model.addAttribute("projectId", projectId);
            model.addAttribute("alsId", alsId);
            model.addAttribute("lbcId", lbcId);

            return "lbcs/lbc";
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
        model.addAttribute("als", ALSImageService.addImages(als));
        model.addAttribute("lb", LBImageService.addImage(lb));
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lbId", lbId);

        return "lbs/lb";
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
            projectService.findLBInProject(projectId, alsId, lbId);
            lb.setId(lbId);
            LB replacement = alsService.restoreLB(lb);
            ALS savedALS = projectService.saveLBAtProject(
                    projectId,
                    alsId,
                    lbId,
                    replacement
            );

            return "redirect:/projects/"
                    + projectId
                    + "/alss/"
                    + savedALS.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId))));

            model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(projectService.findALSInProject(projectId, alsId))));

            model.addAttribute(
                    "lb",
                    LBImageService.addImage(lb)
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

            return "lbs/lb";
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
        ALS als =
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
        ALS als =
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
        ALS als =
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
                        projectExcelExportService.exportToExcel(project)
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
            ProjectDtoMapper.recalculateQuantityALS(project);
            project.getAlsList().forEach(ALSImageService::addImages);
        }
        return project;
    }


    private void prepareLCForm(Model model, LCDTO lc) {
        model.addAttribute("lc", lc);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }

    private void prepareLBCForm(Model model, LBCDTO lbc) {
        model.addAttribute("lbc", lbc);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }


}
