package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.ProjectController;
import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("isAuthenticated()")
public class ProjectRestController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final ALSService alsService;

    public ProjectRestController(ProjectService projectService, ALSService alsService) {
        this.projectService = projectService;
        this.alsService = alsService;
    }
    /**
     * GET /api/v1/projects
     * Получить все проекты
     */
    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        logger.info("Fetching all projects");

        try {
            List<ProjectDTO> projects = projectService.findAll();
            ApiResponse<List<ProjectDTO>> response= ApiResponse.success(projects);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching projects", e);
            ApiResponse<String> response= ApiResponse.error("Error fetching projects",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/create")
    private ResponseEntity<?> createProject() {
        logger.info("Creating new project");
        try {
            ProjectDTO project = projectService.createProject();
            ApiResponse<ProjectDTO> response = ApiResponse.success( "Project created successfully", project);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating project", e);
            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO project) {
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
        try {
            List<ValidationResult> errorProjectList= SizeValidator.validateProject(project);
            if (errorProjectList.isEmpty()) {
                project=projectService.saveProject(project);
                ApiResponse<ProjectDTO> response = ApiResponse.success(project);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }else{
                ApiResponse<List<ValidationResult>> response = ApiResponse.error("Project validation failed", errorProjectList);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating project", e);
            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/save")
    public ResponseEntity<?> saveProject(@RequestBody ProjectDTO project) {
        logger.debug("Saving new project {}", project);
        for(ALSDTO als:project.getAlsList()){
            als=alsService.resizeLC(als);
            als=alsService.resizeLBs(als);
        }
        try {
            List<ValidationResult> errorProjectList= SizeValidator.validateProject(project);
            if (errorProjectList.isEmpty()) {
                project=projectService.saveProject(project);
                ApiResponse<ProjectDTO> response = ApiResponse.success(project);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }else{
                ApiResponse<List<ValidationResult>> response = ApiResponse.error("Project validation failed", errorProjectList);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating project", e);
            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        logger.info("Fetching project with id: {}", id);
        try {
            ProjectDTO project =projectService.findById(id);
            ApiResponse<ProjectDTO> response = ApiResponse.success(project);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            logger.warn("Project not found with id: {}", id);
            ApiResponse<String> response = ApiResponse.error("Project not found with id:"+id,e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    /**
     * DELETE /api/v1/projects/{id}
     * Удалить проект
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        logger.info("Deleting project with id: {}", id);

        try {
            projectService.deleteById(id);
            ApiResponse<Object> response =ApiResponse.success("Project deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting project", e);
            ApiResponse<String> response = ApiResponse.error("Error deleting project", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/v1/projects/{id}/export
     * Экспортировать проект в Excel
     */
    @PostMapping("/{id}/export")
    public ResponseEntity<?> exportProjectToExcel(@PathVariable Long id) {
        logger.info("Exporting project with id: {}", id);

        try {
            ProjectDTO project = projectService.findById(id);
            ByteArrayInputStream excelFile = projectService.exportToExcel(project);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition",
                    "attachment; filename=project_" + id + ".xlsx");
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excelFile));

        } catch (Exception e) {
            logger.error("Error exporting project", e);
            ApiResponse<String> response = ApiResponse.error("Error exporting project",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * POST /api/v1/projects/{id}/add-als
     * Добавить новый ALS к проекту
     */
    @PostMapping("/{id}/add-als")

    public ResponseEntity<?> addALSToProject(@PathVariable Long id) {
        logger.info("Adding new ALS to project id: {}", id);

        try {
            ProjectDTO project = projectService.addNewALSandSaveProject(id);
            ApiResponse<Object> response =ApiResponse.success("ALS added in Project successfully", project);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error adding ALS to project", e);
            ApiResponse<String> response = ApiResponse.error("Error adding ALS to project", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * DELETE /api/v1/projects/{projectId}/alss/{alsId}/delete
     * Удалить ALS из проекта
     */
    @DeleteMapping("/{projectId}/als/{alsId}/delete")

    public ResponseEntity<?> deleteALSFromProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId) {
        logger.info("Deleting ALS {} from project {}", alsId, projectId);

        try {
            ProjectDTO project = projectService.deleteALSandSaveProject(projectId, alsId);
            ApiResponse<Object> response =ApiResponse.success("ALS deleted from Project successfully",project);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting ALS from project", e);
            ApiResponse<String> response = ApiResponse.error("Error deleting ALS from project", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/v1/projects/{projectId}/alss/{alsId}/save
     * Сохранить ALS в проекте с валидацией
     */
    @PostMapping("/{projectId}/alss/{alsId}/save")

    public ResponseEntity<?> saveALSInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @RequestBody ALSDTO als) {
        logger.info("Saving ALS {} in project {}", alsId, projectId);

        try {
            ProjectDTO project = projectService.findById(projectId);
            List<ValidationResult> errorAlsList=SizeValidator.deepValidateALS(als);
             if (!errorAlsList.isEmpty()) {
                ApiResponse<List<ValidationResult>> response = ApiResponse.error("ALS validation failed", errorAlsList);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            ALSDTO saved = projectService.replaceALSandSaveProject(project, als, alsId);
            ApiResponse<Object> response =ApiResponse.success("ALS saved in Project successfully", saved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving ALS in project", e);
            ApiResponse<String> response = ApiResponse.error("Error saving ALS in project", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * POST /api/v1/projects/{projectId}/als/{alsId}/lcs/{lcId}/save
     * Сохранить LC для ALS в проекте с валидацией
     */
    @PostMapping("/{projectId}/alss/{alsId}/lcs/{lcId}/save")

    public ResponseEntity<?> saveLCInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lcId,
            @RequestBody LCDTO lc) {
        logger.info("Saving LC {} for ALS {} in project {}", lcId, alsId, projectId);

        try {
            ProjectDTO project = projectService.findById(projectId);
            ALSDTO als = alsService.findById(alsId);

            // Валидация размеров LC
            List<String> errorList = SizeValidator.getErrorValidateLCSizesList(lc);

            if (!errorList.isEmpty()) {
               ApiResponse<Void> errorResponse = ApiResponse.error("LC validation failed", errorList);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ALSDTO saved = projectService.replaceLCandSaveProject(project, als, alsId, lc);
            ApiResponse<Object> response =ApiResponse.success("LC saved in Project successfully", saved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving LC in project", e);
            ApiResponse<String> response = ApiResponse.error("Error saving LC in project", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/v1/projects/{projectId}/alss/{alsId}/lbs/add
     * Добавить новый LB к ALS в проекте
     */
    @PostMapping("/{projectId}/als/{alsId}/lbs/add")

    public ResponseEntity<?> addLBToALSInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId) {
        logger.info("Adding new LB to ALS {} in project {}", alsId, projectId);

        try {
            ALSDTO als = projectService.addLBAtProject(projectId, alsId);
            ApiResponse<ALSDTO> response =ApiResponse.success("ALS added in Project successfully", als);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error adding LB to ALS", e);
            ApiResponse<String> response = ApiResponse.error("Error adding LB to ALS", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * DELETE /api/v1/projects/{projectId}/als/{alsId}/lbs/{lbId}/delete
     * Удалить LB из ALS в проекте
     */
    @DeleteMapping("/{projectId}/als/{alsId}/lbs/{lbId}/delete")

    public ResponseEntity<?> deleteLBFromALSInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId) {
        logger.info("Deleting LB {} from ALS {} in project {}", lbId, alsId, projectId);

        try {
            ALSDTO als = projectService.deleteLBatProject(projectId, alsId, lbId);
            ApiResponse<ALSDTO> response =ApiResponse.success("ALS deleted from Project successfully", als);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting LB from ALS", e);
            ApiResponse<String> response = ApiResponse.error("Error deleting LB from ALS", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * POST /api/v1/projects/{projectId}/alss/{alsId}/lbs/{lbId}/save
     * Сохранить LB для ALS в проекте с валидацией
     */
    @PostMapping("/{projectId}/als/{alsId}/lbs/{lbId}/save")

    public ResponseEntity<?> saveLBInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @PathVariable Long lbId,
            @RequestBody LBDTO lb) {
        logger.info("Saving LB {} for ALS {} in project {}", lbId, alsId, projectId);

        try {
            ProjectDTO project = projectService.findById(projectId);

            // Валидация размеров LB
            List<String> errorList = SizeValidator.getErrorValidateLBSizesList(lb);

            if (!errorList.isEmpty()) {
                ApiResponse<Void> errorResponse = ApiResponse.error("LB validation failed", errorList);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<Object> result = projectService.saveLBatProject(projectId, alsId, lbId, lb);
            ALSDTO savedALS = (ALSDTO) result.get(0);
            Integer newLbId = (Integer) result.get(1);
            ApiResponse<ALSDTO> response =ApiResponse.success("LB saved in Project successfully", savedALS);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving LB in project", e);
            ApiResponse<String> response = ApiResponse.error("Error saving LB in project", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        }
    }
}
