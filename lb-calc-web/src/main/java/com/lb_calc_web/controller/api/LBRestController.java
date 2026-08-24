package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.service.LBService;
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
@RequestMapping("/api/v1/lbs")
@PreAuthorize("isAuthenticated()")
public class LBRestController {
    private static final Logger logger = LoggerFactory.getLogger(LBRestController.class);
    private final LBService lbService;

    public LBRestController(LBService lbService) {
        this.lbService = lbService;
    }

    /**
     * GET /api/v1/lb
     * Получить все LB
     */
    @GetMapping("/")
    public ResponseEntity<?> getAllLB() {
        logger.info("Fetching all LB");
        try {
            List<LBDTO> lbList = lbService.findAll();
            ApiResponse<List<LBDTO>> response=ApiResponse.success(lbList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching LB", e);
            ApiResponse<String> error = ApiResponse.error("Error fetching LB", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/v1/lbs/{id}
     * Получить LB по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLBById(@PathVariable Long id) {
        logger.info("Fetching LB with id: {}", id);
        try {
            LBDTO lb = lbService.findById(id);
            ApiResponse<LBDTO> response=ApiResponse.success(lb);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("LB not found with id: {}", id);
            ApiResponse<String> error = ApiResponse.error("LB not found with id: " + id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
