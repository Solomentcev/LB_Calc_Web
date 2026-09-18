package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ProjectRestController.class
            );

    private final ProjectService projectService;

    public ProjectRestController(
            ProjectService projectService
    ) {
        this.projectService =
                projectService;
    }

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

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить проект по ID"
    )
    public ResponseEntity<?> getProjectById(
            @PathVariable Long id
    ) {
        try {

            ProjectDTO project =
                    projectService.findById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            project
                    )
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

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

    @PostMapping
    @Operation(
            summary = "Создать проект"
    )
    public ResponseEntity<?> createProject(
            @RequestBody ProjectDTO project
    ) {
        try {

            project.setId(0L);

            ProjectDTO saved =
                    projectService.saveProject(
                            project
                    );

            return ResponseEntity.status(
                            HttpStatus.CREATED
                    )
                    .body(
                            ApiResponse.success(
                                    saved
                            )
                    );

        } catch (ValidationSizeException e) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    "Project validation failed",
                                    e.getErrors()
                            )
                    );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить проект"
    )
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectDTO project
    ) {
        try {

            project.setId(id);

            ProjectDTO saved =
                    projectService.saveProject(
                            project
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            saved
                    )
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );

        } catch (ValidationSizeException e) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    "Project validation failed",
                                    e.getErrors()
                            )
                    );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

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

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/{id}/add-als")
    @Operation(
            summary = "Добавить ALS в проект"
    )
    public ResponseEntity<?> addALSToProject(
            @PathVariable Long id
    ) {
        try {

            ProjectDTO project =
                    projectService
                            .addNewALSandSaveProject(id);

            return ResponseEntity.status(
                            HttpStatus.CREATED
                    )
                    .body(
                            ApiResponse.success(
                                    "ALS added to project successfully",
                                    project
                            )
                    );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

    @DeleteMapping(
            "/{projectId}/als/{alsId}"
    )
    @Operation(
            summary = "Удалить ALS из проекта"
    )
    public ResponseEntity<?> deleteALS(
            @PathVariable Long projectId,
            @PathVariable Long alsId
    ) {
        try {

            ProjectDTO project =
                    projectService
                            .deleteALSandSaveProject(
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

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

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
                            projectService.exportToExcel(
                                    project
                            )
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

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/{id}/alss/{alsId}")
    @Operation(
            summary = "Сохранить ALS в проекте"
    )
    public ResponseEntity<?> saveALS(
            @PathVariable Long id,
            @PathVariable Long alsId,
            @RequestBody ALSDTO als
    ) {
        try {

            ProjectDTO project =
                    projectService.findById(id);

            ALSDTO savedALS =
                    projectService
                            .replaceALSandSaveProject(
                                    project,
                                    als,
                                    alsId
                            );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "ALS saved in project successfully",
                            savedALS
                    )
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );

        } catch (ValidationSizeException e) {

            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    "ALS validation failed",
                                    e.getErrors()
                            )
                    );
        }
    }

    @PostMapping("/{projectId}/als/{alsId}/lbs/add")
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

            return ResponseEntity.status(
                            HttpStatus.CREATED
                    )
                    .body(
                            ApiResponse.success(
                                    "LB added successfully",
                                    als
                            )
                    );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }

    @DeleteMapping(
            "/{projectId}/als/{alsId}/lbs/{lbId}"
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

            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }
}