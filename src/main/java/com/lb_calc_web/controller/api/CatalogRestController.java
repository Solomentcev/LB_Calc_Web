package com.lb_calc_web.controller.api;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API для управления Каталогами (ALS, LB, LC)
 */
@RestController
@RequestMapping("/api/v1/catalogs")
@PreAuthorize("isAuthenticated()")
public class CatalogRestController {
    private static final Logger logger = LoggerFactory.getLogger(CatalogRestController.class);
    private final ALSService alsService;
    private final LBService lbService;
    private final LCService lcService;

    public CatalogRestController(ALSService alsService, LBService lbService, LCService lcService) {
        this.alsService = alsService;
        this.lbService = lbService;
        this.lcService = lcService;
    }

    // ===== ALS ENDPOINTS =====

    /**
     * GET /api/v1/catalogs/als
     * Получить все ALS
     */
    @GetMapping("/als")
    public ResponseEntity<?> getAllALS() {
        logger.info("Fetching all ALS");

        try {
            List<ALSDTO> alsList = alsService.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", alsList);
            response.put("count", alsList.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching ALS", e);
            return buildErrorResponse("Failed to fetch ALS", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/v1/catalogs/als/{id}
     * Получить ALS по ID
     */
    @GetMapping("/als/{id}")
    public ResponseEntity<?> getALSById(@PathVariable Long id) {
        logger.info("Fetching ALS with id: {}", id);

        try {
            ALSDTO als = alsService.findById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", als);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("ALS not found with id: {}", id);
            return buildErrorResponse("ALS not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * POST /api/v1/catalogs/als
     * Создать новый ALS
     */
    @PostMapping("/als")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createALS(@RequestBody @Valid ALSDTO alsDTO) {
        logger.info("Creating new ALS");

        try {
            ALSDTO createdALS = alsService.saveALS(alsDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "ALS created successfully");
            response.put("data", createdALS);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating ALS", e);
            return buildErrorResponse("Failed to create ALS", HttpStatus.BAD_REQUEST);
        }
    }

    // ===== LB ENDPOINTS =====

    /**
     * GET /api/v1/catalogs/lb
     * Получить все LB
     */
    @GetMapping("/lb")
    public ResponseEntity<?> getAllLB() {
        logger.info("Fetching all LB");

        try {
            List<LBDTO> lbList = lbService.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", lbList);
            response.put("count", lbList.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching LB", e);
            return buildErrorResponse("Failed to fetch LB", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/v1/catalogs/lb/{id}
     * Получить LB по ID
     */
    @GetMapping("/lb/{id}")
    public ResponseEntity<?> getLBById(@PathVariable Long id) {
        logger.info("Fetching LB with id: {}", id);

        try {
            LBDTO lb = lbService.findById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", lb);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("LB not found with id: {}", id);
            return buildErrorResponse("LB not found", HttpStatus.NOT_FOUND);
        }
    }

    // ===== LC ENDPOINTS =====

    /**
     * GET /api/v1/catalogs/lc
     * Получить все LC
     */
    @GetMapping("/lc")
    public ResponseEntity<?> getAllLC() {
        logger.info("Fetching all LC");

        try {
            List<LCDTO> lcList = lcService.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", lcList);
            response.put("count", lcList.size());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching LC", e);
            return buildErrorResponse("Failed to fetch LC", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/v1/catalogs/lc/{id}
     * Получить LC по ID
     */
    @GetMapping("/lc/{id}")
    public ResponseEntity<?> getLCById(@PathVariable Long id) {
        logger.info("Fetching LC with id: {}", id);

        try {
            LCDTO lc = lcService.findById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", lc);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("LC not found with id: {}", id);
            return buildErrorResponse("LC not found", HttpStatus.NOT_FOUND);
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
