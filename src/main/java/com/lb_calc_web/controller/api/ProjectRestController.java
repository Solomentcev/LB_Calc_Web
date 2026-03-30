package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.ProjectController;
import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("isAuthenticated()")
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
    /**
     * GET /api/v1/projects
     * Получить все проекты
     */
    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        logger.info("Fetching all projects");

        try {
            List<ProjectDTO> projects = projectService.findAll();
            ApiResponse response= ApiResponse.success(projects);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching projects", e);
            ApiResponse response= ApiResponse.error(e.getMessage());
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
            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
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
            ApiResponse response =ApiResponse.success("Project deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting project", e);
            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
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
            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * POST /api/v1/projects/{id}/als/add
     * Добавить новый ALS к проекту
     */
    @PostMapping("/{id}/als/add")

    public ResponseEntity<?> addALSToProject(@PathVariable Long id) {
        logger.info("Adding new ALS to project id: {}", id);

        try {
            ProjectDTO project = projectService.addNewALSandSaveProject(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "ALS added successfully");
            response.put("data", project);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error adding ALS to project", e);
            return buildErrorResponse("Failed to add ALS: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * DELETE /api/v1/projects/{projectId}/als/{alsId}/delete
     * Удалить ALS из проекта
     */
    @DeleteMapping("/{projectId}/als/{alsId}/delete")

    public ResponseEntity<?> deleteALSFromProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId) {
        logger.info("Deleting ALS {} from project {}", alsId, projectId);

        try {
            ProjectDTO project = projectService.deleteALSandSaveProject(projectId, alsId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "ALS deleted successfully");
            response.put("data", project);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting ALS from project", e);
            return buildErrorResponse("Failed to delete ALS: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * POST /api/v1/projects/{projectId}/als/{alsId}/save
     * Сохранить ALS в проекте с валидацией
     */
    @PostMapping("/{projectId}/als/{alsId}/save")

    public ResponseEntity<?> saveALSInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId,
            @RequestBody ALSDTO als) {
        logger.info("Saving ALS {} in project {}", alsId, projectId);

        try {
            ProjectDTO project = projectService.findById(projectId);

            // Валидация размеров ALS
            List<String> errorALSList = SizeValidator.getErrorValidateALSSizesList(als);
            als = alsService.resizeLC(als);

            List<String> errorLCList = SizeValidator.getErrorValidateLCSizesList(als.getLC());
            als = alsService.resizeLBs(als);

            List<List<String>> errorLBLists = SizeValidator.getErrorValidateLBSizesLists(als);

            if (!errorALSList.isEmpty() || !errorLCList.isEmpty() || !errorLBLists.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Validation errors found");
                errorResponse.put("alsErrors", errorALSList);
                errorResponse.put("lcErrors", errorLCList);
                errorResponse.put("lbErrors", errorLBLists);
                errorResponse.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ALSDTO saved = projectService.replaceALSandSaveProject(project, als, alsId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "ALS saved successfully");
            response.put("data", saved);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving ALS in project", e);
            return buildErrorResponse("Failed to save ALS: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * POST /api/v1/projects/{projectId}/als/{alsId}/lcs/{lcId}/save
     * Сохранить LC для ALS в проекте с валидацией
     */
    @PostMapping("/{projectId}/als/{alsId}/lcs/{lcId}/save")

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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Validation errors found");
                errorResponse.put("errors", errorList);
                errorResponse.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ALSDTO saved = projectService.replaceLCandSaveProject(project, als, alsId, lc);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "LC saved successfully");
            response.put("data", saved);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving LC in project", e);
            return buildErrorResponse("Failed to save LC: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * POST /api/v1/projects/{projectId}/als/{alsId}/lbs/add
     * Добавить новый LB к ALS в проекте
     */
    @PostMapping("/{projectId}/als/{alsId}/lbs/add")

    public ResponseEntity<?> addLBToALSInProject(
            @PathVariable Long projectId,
            @PathVariable Long alsId) {
        logger.info("Adding new LB to ALS {} in project {}", alsId, projectId);

        try {
            ALSDTO als = projectService.addLBAtProject(projectId, alsId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "LB added successfully");
            response.put("data", als);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error adding LB to ALS", e);
            return buildErrorResponse("Failed to add LB: " + e.getMessage(), HttpStatus.BAD_REQUEST);
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

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "LB deleted successfully");
            response.put("data", als);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting LB from ALS", e);
            return buildErrorResponse("Failed to delete LB: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * POST /api/v1/projects/{projectId}/als/{alsId}/lbs/{lbId}/save
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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Validation errors found");
                errorResponse.put("errors", errorList);
                errorResponse.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<Object> result = projectService.saveLBatProject(projectId, alsId, lbId, lb);
            ALSDTO savedALS = (ALSDTO) result.get(0);
            Integer newLbId = (Integer) result.get(1);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "LB saved successfully");
            response.put("als", savedALS);
            response.put("lbId", newLbId);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving LB in project", e);
            return buildErrorResponse("Failed to save LB: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Вспомогательный метод для построения ошибки
     */
    private ResponseEntity<?> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(status).body(errorResponse);
    }


}
