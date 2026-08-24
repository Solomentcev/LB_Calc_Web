package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.service.ALSService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/alss")
@PreAuthorize("isAuthenticated()")
public class ALSRestController {
    private static final Logger logger = LoggerFactory.getLogger(ALSRestController.class);
    private final ALSService alsService;

    public ALSRestController(ALSService alsService) {
        this.alsService = alsService;
    }
    /**
     * GET /api/v1/alss
     * Получить все ALS
     */
    @GetMapping
    public ResponseEntity<?> getAllALS() {
        logger.info("Fetching all ALS");

        try {
            List<ALSDTO> alsList = alsService.findAll();
            ApiResponse<List<ALSDTO>> response = ApiResponse.success("ALS list fetched successfully", alsList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching ALS", e);
            ApiResponse<String> error = ApiResponse.error("Error while fetching ALS",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/v1/alss/{id}
     * Получить ALS по ID
     */
    @GetMapping("/als/{id}")
    public ResponseEntity<?> getALSById(@PathVariable Long id) {
        logger.info("Fetching ALS with id: {}", id);

        try {
            ALSDTO als = alsService.findById(id);
            ApiResponse<ALSDTO> response = ApiResponse.success(als);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("ALS not found with id: {}", id);
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * POST /api/v1/alss
     * Создать новый ALS
     */
    @PostMapping("/als")
    public ResponseEntity<?> createALS(@RequestBody @Valid ALSDTO alsDTO) {
        logger.info("Creating new ALS");

        try {
            ALSDTO createdALS = alsService.saveALS(alsDTO);
            ApiResponse<ALSDTO> response = ApiResponse.success(createdALS);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating ALS", e);
            ApiResponse<String> errorResponse = ApiResponse.error("Error creating ALS",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
