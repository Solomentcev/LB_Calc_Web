package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.ControlModule;
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
import com.lb_calc_web.domain.geometry.SizeBounds;
import com.lb_calc_web.service.factory.TypeLbFactory;
import org.springframework.core.env.Environment;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.ProjectCatalogFilter;
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

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** MVC-контроллер создания, редактирования, сохранения и экспорта проектов. */
@Controller
@RequestMapping("/projects")
public class ProjectController
        extends BaseCatalogController {

    /** Определяет контекст отображения страницы проекта. */
    @ModelAttribute("pageContext")
    public String pageContext() {
        return "PROJECT";
    }

    private final ProjectService projectService;
    private final ALSService alsService;
    private final ProjectExcelExportService projectExcelExportService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;
    private final Environment environment;

    public ProjectController(
            ProjectService projectService,
            ALSService alsService,
            ProjectExcelExportService projectExcelExportService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds,
            Environment environment
    ) {
        this.projectService = projectService;
        this.alsService = alsService;
        this.projectExcelExportService = projectExcelExportService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
        this.environment = environment;
    }

    /**
     * Список всех проектов.
     */
    @GetMapping
    public String projects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String createdByFirstName,
            @RequestParam(required = false) String createdByLastName,
            @RequestParam(required = false) LocalDate createdAtFrom,
            @RequestParam(required = false) LocalDate createdAtTo,
            @RequestParam(required = false) String updatedByFirstName,
            @RequestParam(required = false) String updatedByLastName,
            @RequestParam(required = false) LocalDate updatedAtFrom,
            @RequestParam(required = false) LocalDate updatedAtTo,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "company", "description",
                        "createdBy", "createdAt", "updatedBy", "updatedAt"));
        ProjectCatalogFilter filter = new ProjectCatalogFilter(
                id, name, company, description,
                createdByFirstName, createdByLastName, createdAtFrom, createdAtTo,
                updatedByFirstName, updatedByLastName, updatedAtFrom, updatedAtTo);
        var result = projectService.findPage(filter, pageable).map(ProjectDtoMapper::toDto);
        model.addAttribute("projects", result.getContent());
        model.addAttribute("filters", filter);
        model.addAttribute("page", result);
        model.addAttribute("sort", Set.of("id", "name", "company", "description",
                "createdBy", "createdAt", "updatedBy", "updatedAt").contains(sort) ? sort : "id");
        model.addAttribute("direction", pageable.getSort().stream()
                .findFirst().map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("sortOptions", CatalogPageSupport.sortOptions(
                "id", "ID", "name", "Название", "company", "Компания",
                "description", "Описание", "createdBy", "Создатель",
                "createdAt", "Дата создания", "updatedBy", "Последний редактор",
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
     * Пересчитывает вложенные ALS и обновляет форму проекта без сохранения.
     */
    @PostMapping("/{id}/refresh")
    public String refreshProject(@PathVariable Long id,
                                 @ModelAttribute("project") ProjectDTO project,
                                 Model model) {
        project.setId(id);
        populateServerMetadata(project, id);
        populateProjectComposition(project, id);
        try {
            com.lb_calc_web.domain.model.Project domain =
                    ProjectDtoMapper.toDomain(project, dto -> {
                        ALS existing = dto.getId() != null && dto.getId() > 0
                                ? alsService.findById(dto.getId()) : null;
                        ALS restored = ALSDtoMapper.toDomain(
                                dto,
                                candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                                candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                                equipmentCatalogService::findById,
                                sizeBounds
                        );
                        return alsService.prepareConfiguration(existing, restored, dto.isEquipmentSelectionProvided());
                    });
            project = ProjectDtoMapper.toDto(projectService.previewProject(domain));
        } catch (ValidationSizeException exception) {
            model.addAttribute("projectErrors",
                    ValidationErrorFormatter.format(exception.getValidationResult()));
        }
        model.addAttribute("project", addALSImages(project));
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
            populateProjectComposition(project, id);

            com.lb_calc_web.domain.model.Project domain =
                    com.lb_calc_web.mapper.dto.ProjectDtoMapper.toDomain(
                            project,
                            dto -> {
                        ALS existing = dto.getId() != null && dto.getId() > 0
                                ? alsService.findById(dto.getId()) : null;
                        ALS restored = ALSDtoMapper.toDomain(
                                dto,
                                candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                                candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                                equipmentCatalogService::findById,
                                sizeBounds
                        );
                        return alsService.prepareConfiguration(existing, restored, dto.isEquipmentSelectionProvided());
                    }
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
     * Восстанавливает состав проекта, который не редактируется на странице проекта.
     *
     * <p>Для существующего проекта состав берётся из сохранённой доменной модели.
     * Для нового проекта создаётся начальная конфигурация АКХ.</p>
     *
     * @param dto DTO проекта из формы
     * @param projectId идентификатор проекта
     */
    private void populateProjectComposition(ProjectDTO dto, Long projectId) {
        if (projectId != null && projectId > 0) {
            ProjectDTO persisted = ProjectDtoMapper.toDto(projectService.findById(projectId));
            dto.setAlsList(persisted.getAlsList());
        } else if (dto.getAlsList() == null || dto.getAlsList().isEmpty()) {
            dto.setAlsList(List.of(ALSDtoMapper.toDto(alsService.createALS())));
        }
        ProjectDtoMapper.recalculateQuantityALS(dto);
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
                addALSImage(als)
        );
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);

        return "alss/als";
    }

    /**
     * Пересчитывает ALS проекта и обновляет форму без сохранения проекта.
     */
    @PostMapping("/{projectId}/alss/{alsId}/refresh")
    public String refreshALS(@PathVariable Long projectId, @PathVariable Long alsId,
                             @ModelAttribute("als") ALSDTO als, Model model) {
        ALS persistedALS = projectService.findALSInProject(projectId, alsId);
        als.setId(alsId);
        // Состав управляющего модуля редактируется в отдельной форме.
        // Не восстанавливаем его состояние из скрытых полей страницы ALS.
        ALSDTO persisted = ALSDtoMapper.toDto(persistedALS);
        als.setLC(persisted.getLC());
        als.setLBC(persisted.getLBC());
        com.lb_calc_web.domain.model.Project preview =
                projectService.findById(projectId);
        try {
            ALS restored = ALSDtoMapper.toDomain(
                    als,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            ALS replacement = alsService.prepareConfiguration(persistedALS, restored, als.isEquipmentSelectionProvided());
            preview = projectService.previewALSReplacement(projectId, alsId, replacement);
            als = ALSDtoMapper.toDto(replacement);
        } catch (ValidationSizeException exception) {
            model.addAttribute("ALSErrors",
                    ValidationErrorFormatter.format(exception.getValidationResult()));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("ALSErrors", List.of(exception.getMessage()));
        }
        model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(preview)));
        model.addAttribute("als", addALSImage(als));
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
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
            ALS persistedALS = projectService.findALSInProject(projectId, alsId);
            als.setId(alsId);
            // Состав управляющего модуля редактируется в отдельной форме.
            // Сохраняем актуальную серверную конфигурацию, а не скрытые поля формы ALS.
            ALSDTO persisted = ALSDtoMapper.toDto(persistedALS);
            als.setLC(persisted.getLC());
            als.setLBC(persisted.getLBC());
            ALS restored = ALSDtoMapper.toDomain(
                    als,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            ALS replacement = alsService.prepareConfiguration(persistedALS, restored, als.isEquipmentSelectionProvided());
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

            model.addAttribute("als", addALSImage(als));

            model.addAttribute(
                    "project",
                    addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId)))
            );
            model.addAttribute("projectId", projectId);
            model.addAttribute("alsId", alsId);

            return "alss/als";
        } catch (IllegalArgumentException e) {
            model.addAttribute("ALSErrors", List.of(e.getMessage()));
            model.addAttribute("als", addALSImage(als));
            model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(projectService.findById(projectId))));
            model.addAttribute("projectId", projectId);
            model.addAttribute("alsId", alsId);
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

        LC lcDomain = projectService.findLCInProject(projectId, alsId, lcId);

        model.addAttribute("project", addALSImages(project));
        model.addAttribute("als", addALSImage(als));
        LCDTO lcForm = toDtoWithImage(lcDomain);
        model.addAttribute("lc", lcForm);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lcId", lcId);

        return "lcs/lc";
    }

    /** Обновляет комплектацию LC проекта без сохранения проекта или ALS. */
    @PostMapping("/{projectId}/alss/{alsId}/lcs/{lcId}/refresh")
    public String refreshLC(@PathVariable Long projectId, @PathVariable Long alsId,
                            @PathVariable Long lcId, @ModelAttribute("lc") LCDTO lc, Model model) {
        ALS sourceALS = projectService.findALSInProject(projectId, alsId);
        projectService.findLCInProject(projectId, alsId, lcId);
        lc.setId(lcId);
        com.lb_calc_web.domain.model.Project preview = projectService.findById(projectId);
        ALS updatedALS = sourceALS;
        LCDTO lcForm = lc;
        try {
            com.lb_calc_web.domain.model.LC replacement = LCDtoMapper.toDomain(
                    lc, equipmentCatalogService::findById,
                    sizeBounds);
            alsService.prepareForReplacement(sourceALS, replacement, lc.isEquipmentProvided());
            ALS candidateALS = alsService.replaceLC(sourceALS.copy(), replacement);
            preview = projectService.previewALSReplacement(projectId, alsId, candidateALS);
            updatedALS = candidateALS;
            lcForm = toDtoWithImage(replacement);
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        }
        model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(preview)));
        model.addAttribute("als", toDtoWithImages(updatedALS));
        model.addAttribute("lc", lcForm);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
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
        LC replacement = null;
        try {
            lc.setId(lcId);
            projectService.findLCInProject(projectId, alsId, lcId);
            replacement = LCDtoMapper.toDomain(
                    lc, equipmentCatalogService::findById,
                    sizeBounds);
            ALS savedALS = projectService.saveLCAtProject(
                    projectId,
                    alsId,
                    lcId,
                    replacement,
                    lc.isEquipmentProvided()
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
                    toDtoWithImages(projectService.findALSInProject(projectId, alsId))
            );

            LCDTO lcForm = replacement == null ? lc : toDtoWithImage(replacement);
            model.addAttribute("lc", lcForm);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());

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

        LBC lbcDomain = projectService.findLBCInProject(projectId, alsId, lbcId);

        model.addAttribute("project", addALSImages(project));
        model.addAttribute("als", addALSImage(als));
        LBCDTO lbcForm = toDtoWithImage(lbcDomain);
        model.addAttribute("lbc", lbcForm);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lbcId", lbcId);

        return "lbcs/lbc";
    }

    /** Обновляет комплектацию LBC проекта без сохранения проекта или ALS. */
    @PostMapping("/{projectId}/alss/{alsId}/lbcs/{lbcId}/refresh")
    public String refreshLBC(@PathVariable Long projectId, @PathVariable Long alsId,
                             @PathVariable Long lbcId, @ModelAttribute("lbc") LBCDTO lbc, Model model) {
        ALS sourceALS = projectService.findALSInProject(projectId, alsId);
        projectService.findLBCInProject(projectId, alsId, lbcId);
        lbc.setId(lbcId);
        com.lb_calc_web.domain.model.Project preview = projectService.findById(projectId);
        ALS updatedALS = sourceALS;
        LBCDTO lbcForm = lbc;
        try {
            com.lb_calc_web.domain.model.LBC replacement = LBCDtoMapper.toDomain(
                    lbc, TypeLbFactory.forLBC(environment, lbc.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds);
            alsService.prepareForReplacement(sourceALS, replacement, lbc.isEquipmentProvided());
            ALS candidateALS = alsService.replaceLBC(sourceALS.copy(), replacement);
            preview = projectService.previewALSReplacement(projectId, alsId, candidateALS);
            updatedALS = candidateALS;
            lbcForm = toDtoWithImage(replacement);
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        }
        model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(preview)));
        model.addAttribute("als", toDtoWithImages(updatedALS));
        model.addAttribute("lbc", lbcForm);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
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
        LBC replacement = null;
        try {
            lbc.setId(lbcId);
            projectService.findLBCInProject(projectId, alsId, lbcId);
            replacement = LBCDtoMapper.toDomain(
                    lbc, TypeLbFactory.forLBC(environment, lbc.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds);
            ALS savedALS = projectService.saveLBCAtProject(
                    projectId,
                    alsId,
                    lbcId,
                    replacement,
                    lbc.isEquipmentProvided()
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

            model.addAttribute("als", toDtoWithImages(projectService.findALSInProject(projectId, alsId)));

            LBCDTO lbcForm = replacement == null ? lbc : toDtoWithImage(replacement);
            model.addAttribute("lbc", lbcForm);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());

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
        model.addAttribute("als", addALSImage(als));
        model.addAttribute("lb", addLBImage(lb));
        model.addAttribute("projectId", projectId);
        model.addAttribute("alsId", alsId);
        model.addAttribute("lbId", lbId);

        return "lbs/lb";
    }

    /**
     * Пересчитывает LB проекта и обновляет форму без сохранения ALS.
     */
    @PostMapping("/{projectId}/alss/{alsId}/lbs/{lbId}/refresh")
    public String refreshLB(@PathVariable Long projectId, @PathVariable Long alsId,
                            @PathVariable Long lbId, @ModelAttribute("lb") LBDTO lb,
                            Model model) {
        projectService.findLBInProject(projectId, alsId, lbId);
        lb.setId(lbId);
        com.lb_calc_web.domain.model.Project preview = projectService.findById(projectId);
        ALS sourceALS = projectService.findALSInProject(projectId, alsId);
        ALS updatedALS = sourceALS;
        try {
            LB replacement = LBDtoMapper.toDomain(
                    lb, TypeLbFactory.forLB(environment, lb.getType()));
            ALS candidateALS = alsService.replaceLB(sourceALS.copy(), lbId, replacement);
            preview = projectService.previewALSReplacement(projectId, alsId, candidateALS);
            updatedALS = candidateALS;
            lb = LBDtoMapper.toDto(replacement);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errors", List.of("Проверьте введённые параметры модуля."));
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        }
        model.addAttribute("project", addALSImages(ProjectDtoMapper.toDto(preview)));
        model.addAttribute("als", toDtoWithImages(updatedALS));
        model.addAttribute("lb", addLBImage(lb));
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
            LB replacement = LBDtoMapper.toDomain(
                    lb, TypeLbFactory.forLB(environment, lb.getType()));
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

            model.addAttribute("als", toDtoWithImages(projectService.findALSInProject(projectId, alsId)));

            model.addAttribute(
                    "lb",
                    addLBImage(lb)
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
     * Преобразует LBC в LC и отдельный LB в выбранной ALS проекта.
     */
    @PostMapping("/{projectId}/alss/{alsId}/replaceLC")
    public String replaceLBCWithLC(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {
        ALS als = projectService.replaceWithNewLCAtProject(projectId, alsId);
        return "redirect:/projects/" + projectId + "/alss/" + als.getId();
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
                        projectExcelExportService.exportToExcel(projectService.findById(id))
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
            for (int index = 0; index < project.getAlsList().size(); index++) {
                ALSDTO als = project.getAlsList().get(index);
                try {
                    ALS existing = als.getId() != null && als.getId() > 0
                            ? alsService.findById(als.getId()) : null;
                    ALS restored = ALSDtoMapper.toDomain(
                            als,
                            candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                            candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                            equipmentCatalogService::findById,
                            sizeBounds
                    );
                    restored = alsService.prepareConfiguration(existing, restored, als.isEquipmentSelectionProvided());
                    project.getAlsList().set(index, toDtoWithImages(restored));
                } catch (ValidationSizeException | IllegalArgumentException | NoSuchElementException exception) {
                    // Для некорректной промежуточной формы изображение не формируется.
                }
            }
            ProjectDtoMapper.recalculateQuantityALS(project);
        }
        return project;
    }

    private ALSDTO addALSImage(ALSDTO dto) {
        try {
            ALS existing = dto.getId() != null && dto.getId() > 0
                    ? alsService.findById(dto.getId()) : null;
            ALS restored = ALSDtoMapper.toDomain(
                    dto,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            return toDtoWithImages(alsService.prepareConfiguration(existing, restored, dto.isEquipmentSelectionProvided()));
        } catch (ValidationSizeException | IllegalArgumentException | NoSuchElementException exception) {
            return dto;
        }
    }

    private ALSDTO toDtoWithImages(ALS domain) {
        ALSDTO dto = ALSDtoMapper.toDto(domain);
        dto.setStringALSImage(ALSImageService.getStringALSImage(domain));
        int lbIndex = 0;
        for (com.lb_calc_web.domain.model.Module module : domain.getModules()) {
            if (module instanceof LB lb) {
                dto.getLbList().get(lbIndex++).setStringLBImage(LBImageService.getStringLBImage(lb));
            } else if (module instanceof LC lc) {
                dto.getLC().setStringLCImage(LCImageService.getStringLCImage(lc));
            } else if (module instanceof LBC lbc) {
                dto.getLBC().setStringLBCImage(LBCImageService.getStringLBCImage(lbc));
            }
        }
        return dto;
    }

    private LBDTO addLBImage(LBDTO dto) {
        try {
            return toDtoWithImage(LBDtoMapper.toDomain(
                    dto, TypeLbFactory.forLB(environment, dto.getType())));
        } catch (ValidationSizeException | IllegalArgumentException exception) {
            return dto;
        }
    }

    private LCDTO toDtoWithImage(LC domain) {
        LCDTO dto = LCDtoMapper.toDto(domain);
        dto.setStringLCImage(LCImageService.getStringLCImage(domain));
        return dto;
    }

    private LBCDTO toDtoWithImage(LBC domain) {
        LBCDTO dto = LBCDtoMapper.toDto(domain);
        dto.setStringLBCImage(LBCImageService.getStringLBCImage(domain));
        return dto;
    }

    private LBDTO toDtoWithImage(LB domain) {
        LBDTO dto = LBDtoMapper.toDto(domain);
        dto.setStringLBImage(LBImageService.getStringLBImage(domain));
        return dto;
    }



}
