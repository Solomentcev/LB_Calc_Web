package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.handler.ValidationSizeException;
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

    public ProjectRestController(
            ProjectService projectService
    ) {
        this.projectService = projectService;
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
                projectService.findAll();

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
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            projectService.findById(id)
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
                        projectService.createProject()
                )
        );
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
        try {
            /*
             * Новый проект всегда создаётся как новый объект.
             */
            project.setId(0L);

            ProjectDTO saved =
                    projectService.saveProject(project);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(saved)
                    );

        } catch (ValidationSizeException e) {

            return badRequest(
                    "Project validation failed",
                    e.getErrors()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            return badRequest(
                    e.getMessage()
            );
        }
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
        try {
            /*
             * ID из URL является источником истины.
             */
            project.setId(id);

            ProjectDTO saved =
                    projectService.saveProject(project);

            return ResponseEntity.ok(
                    ApiResponse.success(saved)
            );

        } catch (NoSuchElementException e) {

            return notFound(e);

        } catch (ValidationSizeException e) {

            return badRequest(
                    "Project validation failed",
                    e.getErrors()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            return badRequest(
                    e.getMessage()
            );
        }
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
        try {
            projectService.deleteById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Project deleted successfully"
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            projectService.findALSInProject(
                                    projectId,
                                    alsId
                            )
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            ProjectDTO project =
                    projectService.addNewALSandSaveProject(
                            projectId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    "ALS added to project successfully",
                                    project
                            )
                    );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            als.setId(alsId);

            ProjectDTO project =
                    projectService.findById(projectId);

            ALSDTO saved =
                    projectService.replaceALSandSaveProject(
                            project,
                            als,
                            alsId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "ALS updated successfully",
                            saved
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);

        } catch (ValidationSizeException e) {

            return badRequest(
                    "ALS validation failed",
                    e.getErrors()
            );
        }
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
        try {
            ProjectDTO project =
                    projectService.deleteALSandSaveProject(
                            projectId,
                            alsId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "ALS deleted from project successfully",
                            project
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            LCDTO lc =
                    projectService.findLCInProject(
                            projectId,
                            alsId,
                            lcId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(lc)
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            lc.setId(lcId);

            LCDTO saved =
                    projectService.saveLCAtProject(
                            projectId,
                            alsId,
                            lcId,
                            lc
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LC updated successfully",
                            saved
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);

        } catch (ValidationSizeException e) {

            return badRequest(
                    "LC validation failed",
                    e.getErrors()
            );
        }
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
        try {
            ALSDTO saved =
                    projectService.replaceWithNewLBCAtProject(
                            projectId,
                            alsId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LBC added to ALS successfully",
                            saved
                    )
            );
        } catch (NoSuchElementException e) {
            return notFound(e);
        } catch (ValidationSizeException e) {
            return badRequest(
                    "LBC validation failed",
                    e.getErrors()
            );
        }
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
        try {
            LBCDTO lbc =
                    projectService.findLBCInProject(
                            projectId,
                            alsId,
                            lbcId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(lbc)
            );
        } catch (NoSuchElementException e) {
            return notFound(e);
        }
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
        try {
            lbc.setId(lbcId);

            LBCDTO saved =
                    projectService.saveLBCAtProject(
                            projectId,
                            alsId,
                            lbcId,
                            lbc
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LBC updated successfully",
                            saved
                    )
            );
        } catch (NoSuchElementException e) {
            return notFound(e);
        } catch (ValidationSizeException e) {
            return badRequest(
                    "LBC validation failed",
                    e.getErrors()
            );
        }
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
        try {
            LBDTO lb =
                    projectService.findLBInProject(
                            projectId,
                            alsId,
                            lbId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(lb)
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            lb.setId(lbId);

            LBDTO saved =
                    projectService.saveLBAtProject(
                            projectId,
                            alsId,
                            lbId,
                            lb
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LB updated successfully",
                            saved
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);

        } catch (ValidationSizeException e) {

            return badRequest(
                    "LB validation failed",
                    e.getErrors()
            );
        }
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
        try {
            ALSDTO als =
                    projectService.addLBAtProject(
                            projectId,
                            alsId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            ApiResponse.success(
                                    "LB added successfully",
                                    als
                            )
                    );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            ALSDTO als =
                    projectService.deleteLBatProject(
                            projectId,
                            alsId,
                            lbId
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LB deleted successfully",
                            als
                    )
            );

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
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
        try {
            ProjectDTO project =
                    projectService.findById(id);

            InputStreamResource file =
                    new InputStreamResource(
                            projectService.exportToExcel(project)
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

        } catch (NoSuchElementException e) {

            return notFound(e);
        }
    }

    private ResponseEntity<?> notFound(
            NoSuchElementException e
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.error(
                                e.getMessage()
                        )
                );
    }

    private ResponseEntity<?> badRequest(
            String message
    ) {
        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse.error(message)
                );
    }

    private ResponseEntity<?> badRequest(
            String message,
            List<String> errors
    ) {
        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse.error(
                                message,
                                errors
                        )
                );
    }
}