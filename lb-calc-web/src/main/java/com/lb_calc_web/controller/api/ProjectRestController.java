package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.ControlModule;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.domain.geometry.SizeBounds;
import org.springframework.core.env.Environment;
import com.lb_calc_web.service.export.ProjectExcelExportService;
import com.lb_calc_web.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/** REST-контроллер управления проектами и их конфигурациями ALS. */
@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "Projects",
        description = "API проектов"
)
@SecurityRequirement(name = "bearerAuth")
public class ProjectRestController {

    private final ProjectService projectService;
    private final ALSService alsService;
    private final ProjectExcelExportService projectExcelExportService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;
    private final Environment environment;

    public ProjectRestController(
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
     * Получить список проектов.
     */
    @GetMapping
    @Operation(
            summary = "Получить список проектов"
    )
    public ResponseEntity<?> getAllProjects() {

        List<ProjectDTO> projects =
                ProjectDtoMapper.toDtoList(projectService.findAll());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Project list fetched successfully",
                        projects
                )
        );
    }

    /**
     * Получить проект по ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить проект по ID"
    )
    public ResponseEntity<?> getProjectById(
            @PathVariable Long id
    ) {

            return ResponseEntity.ok(
                    ApiResponse.success(
                            ProjectDtoMapper.toDto(projectService.findById(id))
                    )
            );

    }

    /**
     * Получить шаблон нового проекта.
     */
    @GetMapping("/create")
    @Operation(
            summary = "Получить шаблон нового проекта"
    )
    public ResponseEntity<?> createProject() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        createProjectDto()
                )
        );
    }

    private ProjectDTO createProjectDto() {
        ProjectDTO dto = ProjectDtoMapper.toDto(projectService.createProject());
        dto.setId(0L);
        return dto;
    }

    /**
     * Создать проект.
     */
    @PostMapping
    @Operation(
            summary = "Создать проект"
    )
    public ResponseEntity<?> createProject(
            @RequestBody ProjectDTO project
    ) {

            /*
             * Новый проект всегда создаётся как новый объект.
             */
            project.setId(0L);

            Project domain = ProjectDtoMapper.toDomain(
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
            Project saved = projectService.saveProject(domain);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(ProjectDtoMapper.toDto(saved)));

    }

    /**
     * Обновить проект.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить проект"
    )
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectDTO project
    ) {

            /*
             * ID из URL является источником истины.
             */
            project.setId(id);

            Project domain = ProjectDtoMapper.toDomain(
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
            Project saved = projectService.saveProject(domain);

            return ResponseEntity.ok(
                    ApiResponse.success(ProjectDtoMapper.toDto(saved))
            );

    }

    /**
     * Удалить проект.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить проект"
    )
    public ResponseEntity<?> deleteProject(
            @PathVariable Long id
    ) {

            projectService.deleteById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Project deleted successfully"
                    )
            );

    }

    /**
     * Получить ALS проекта.
     */
    @GetMapping("/{projectId}/als/{alsId}")
    @Operation(
            summary = "Получить ALS проекта"
    )
    public ResponseEntity<?> getALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {

            return ResponseEntity.ok(
                    ApiResponse.success(
                            ALSDtoMapper.toDto(projectService.findALSInProject(projectId, alsId))
                    )
            );

    }

    /**
     * Добавить ALS в проект.
     */
    @PostMapping("/{projectId}/als")
    @Operation(
            summary = "Добавить ALS в проект"
    )
    public ResponseEntity<?> addALS(
            @PathVariable Long projectId
    ) {

            Project project = projectService.addNewALSandSaveProject(projectId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    "ALS added to project successfully",
                                    ProjectDtoMapper.toDto(project)
                            )
                    );

    }

    /**
     * Обновить ALS внутри проекта.
     */
    @PutMapping("/{projectId}/als/{alsId}")
    @Operation(
            summary = "Обновить ALS в проекте"
    )
    public ResponseEntity<?> updateALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @RequestBody ALSDTO als
    ) {

            projectService.findALSInProject(projectId, alsId);
            als.setId(alsId);
            ALS existing = projectService.findALSInProject(projectId, alsId);
            ALS restored = ALSDtoMapper.toDomain(
                    als,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            ALS replacement = alsService.prepareConfiguration(existing, restored, als.isEquipmentSelectionProvided());
            ALS saved = projectService.replaceALSandSaveProject(
                    projectId,
                    alsId,
                    replacement
            );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "ALS updated successfully",
                            ALSDtoMapper.toDto(saved)
                    )
            );

    }

    /**
     * Удалить ALS из проекта.
     */
    @DeleteMapping("/{projectId}/als/{alsId}")
    @Operation(
            summary = "Удалить ALS из проекта"
    )
    public ResponseEntity<?> deleteALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {

            Project project = projectService.deleteALSandSaveProject(
                    projectId,
                    alsId
            );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "ALS deleted from project successfully",
                            ProjectDtoMapper.toDto(project)
                    )
            );

    }

    /**
     * Получить LC проекта.
     */
    @GetMapping(
            "/{projectId}/als/{alsId}/lc/{lcId}"
    )
    @Operation(
            summary = "Получить LC ALS проекта"
    )
    public ResponseEntity<?> getLC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lcId
    ) {

            LCDTO lc =
                    LCDtoMapper.toDto(projectService.findLCInProject(projectId, alsId, lcId));

            return ResponseEntity.ok(
                    ApiResponse.success(lc)
            );

    }

    /**
     * Обновить LC проекта.
     */
    @PutMapping(
            "/{projectId}/als/{alsId}/lc/{lcId}"
    )
    @Operation(
            summary = "Обновить LC ALS проекта"
    )
    public ResponseEntity<?> updateLC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lcId,
            @RequestBody LCDTO lc
    ) {

            projectService.findLCInProject(projectId, alsId, lcId);
            lc.setId(lcId);
            ALS sourceALS = projectService.findALSInProject(projectId, alsId);
            LC replacement = LCDtoMapper.toDomain(
                    lc,
                    equipmentCatalogService::findById,
                    sourceALS.getControlModule() instanceof LC
                            ? sourceALS.getControlModule().getControlConfiguration() : null,
                    sizeBounds
            );
            ALS savedALS = projectService.saveLCAtProject(
                    projectId,
                    alsId,
                    lcId,
                    replacement,
                    lc.isEquipmentProvided()
            );
            LCDTO saved = LCDtoMapper.toDto(savedALS.getLC());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LC updated successfully",
                            saved
                    )
            );

    }

    /**
     * Заменить control-модуль ALS проекта новым LBC.
     */
    @PostMapping(
            "/{projectId}/als/{alsId}/replace-lbc"
    )
    @Operation(
            summary = "Заменить control-модуль ALS на LBC"
    )
    public ResponseEntity<?> replaceLBC(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {

            ALS saved =
                    projectService.replaceWithNewLBCAtProject(
                            projectId,
                            alsId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LBC added to ALS successfully",
                            ALSDtoMapper.toDto(saved)
                    )
            );

    }

    /**
     * Получить LBC проекта.
     */
    @GetMapping(
            "/{projectId}/als/{alsId}/lbc/{lbcId}"
    )
    @Operation(
            summary = "Получить LBC ALS проекта"
    )
    public ResponseEntity<?> getLBC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbcId
    ) {

            LBCDTO lbc =
                    LBCDtoMapper.toDto(projectService.findLBCInProject(projectId, alsId, lbcId));

            return ResponseEntity.ok(
                    ApiResponse.success(lbc)
            );

    }

    /**
     * Обновить LBC проекта.
     */
    @PutMapping(
            "/{projectId}/als/{alsId}/lbc/{lbcId}"
    )
    @Operation(
            summary = "Обновить LBC ALS проекта"
    )
    public ResponseEntity<?> updateLBC(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            @RequestBody LBCDTO lbc
    ) {

            projectService.findLBCInProject(projectId, alsId, lbcId);
            lbc.setId(lbcId);
            ALS sourceALS = projectService.findALSInProject(projectId, alsId);
            LBC replacement = LBCDtoMapper.toDomain(
                    lbc,
                    TypeLbFactory.forLBC(environment, lbc.getType()),
                    equipmentCatalogService::findById,
                    sourceALS.getControlModule() instanceof LBC
                            ? sourceALS.getControlModule().getControlConfiguration() : null,
                    sizeBounds
            );
            ALS savedALS = projectService.saveLBCAtProject(
                    projectId,
                    alsId,
                    lbcId,
                    replacement,
                    lbc.isEquipmentProvided()
            );
            LBCDTO saved = LBCDtoMapper.toDto(savedALS.getLBC());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LBC updated successfully",
                            saved
                    )
            );

    }

    /**
     * Получить LB проекта.
     */
    @GetMapping(
            "/{projectId}/als/{alsId}/lb/{lbId}"
    )
    @Operation(
            summary = "Получить LB ALS проекта"
    )
    public ResponseEntity<?> getLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {

            LBDTO lb =
                    LBDtoMapper.toDto(projectService.findLBInProject(projectId, alsId, lbId));

            return ResponseEntity.ok(
                    ApiResponse.success(lb)
            );

    }

    /**
     * Обновить LB проекта.
     */
    @PutMapping(
            "/{projectId}/als/{alsId}/lb/{lbId}"
    )
    @Operation(
            summary = "Обновить LB ALS проекта"
    )
    public ResponseEntity<?> updateLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId,
            @RequestBody LBDTO lb
    ) {

            lb.setId(lbId);

            ALSDTO currentALS =
                    ALSDtoMapper.toDto(projectService.findALSInProject(projectId, alsId));
            int lbIndex = -1;
            for (int index = 0; index < currentALS.getLbList().size(); index++) {
                if (Objects.equals(currentALS.getLbList().get(index).getId(), lbId)) {
                    lbIndex = index;
                    break;
                }
            }
            if (lbIndex < 0) {
                throw new NoSuchElementException(
                        "LB с id " + lbId + " не найден в ALS " + alsId
                );
            }

            LB replacement = LBDtoMapper.toDomain(
                    lb, TypeLbFactory.forLB(environment, lb.getType()));
            ALS savedALS = projectService.saveLBAtProject(
                    projectId,
                    alsId,
                    lbId,
                    replacement
            );
            LBDTO saved = ALSDtoMapper.toDto(savedALS).getLbList().get(lbIndex);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LB updated successfully",
                            saved
                    )
            );

    }

    /**
     * Добавить LB в ALS проекта.
     */
    @PostMapping(
            "/{projectId}/als/{alsId}/lb"
    )
    @Operation(
            summary = "Добавить LB в ALS проекта"
    )
    public ResponseEntity<?> addLB(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {

            ALS als =
                    projectService.addLBAtProject(
                            projectId,
                            alsId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    "LB added successfully",
                                    ALSDtoMapper.toDto(als)
                            )
                    );

    }

    /**
     * Удалить LB из ALS проекта.
     */
    @DeleteMapping(
            "/{projectId}/als/{alsId}/lb/{lbId}"
    )
    @Operation(
            summary = "Удалить LB из ALS проекта"
    )
    public ResponseEntity<?> deleteLB(
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

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LB deleted successfully",
                            ALSDtoMapper.toDto(als)
                    )
            );

    }

    /**
     * Экспортировать проект в Excel.
     */
    @PostMapping("/{id}/export")
    @Operation(
            summary = "Экспортировать проект в Excel"
    )
    public ResponseEntity<?> exportProjectToExcel(
            @PathVariable Long id
    ) {

            InputStreamResource file =
                    new InputStreamResource(
                            projectExcelExportService.exportToExcel(projectService.findById(id))
                    );

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=project_"
                                    + id
                                    + ".xlsx"
                    )
                    .contentType(
                            MediaType.parseMediaType(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                    )
                    .body(file);

    }

}
