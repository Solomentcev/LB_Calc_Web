package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.service.LCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lcs")
@PreAuthorize("isAuthenticated()")
public class LCRestController {
    private static final Logger logger = LoggerFactory.getLogger(LCRestController.class);
    private final LCService lcService;

    public LCRestController(LCService lcService) {
        this.lcService = lcService;
    }

    /**
     * GET /api/v1/lcs
     * Получить все LC
     */
    @GetMapping
    public ResponseEntity<?> getAllLC() {
        logger.info("Fetching all LC");

        try {
            List<LCDTO> lcList = lcService.findAll();
            ApiResponse<List<LCDTO>> response = ApiResponse.success("LC list fetched successfully", lcList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching LC", e);
            ApiResponse<Void> error = ApiResponse.error("Error fetching LC"+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/v1/lcs/{id}
     * Получить LC по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLCById(@PathVariable Long id) {
        logger.info("Fetching LC with id: {}", id);

        try {
            LCDTO lc = lcService.findById(id);
            ApiResponse<LCDTO> response = ApiResponse.success(lc);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("LC not found with id: {}", id);
            ApiResponse<String> error = ApiResponse.error("LC not found with id: "+id,e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
