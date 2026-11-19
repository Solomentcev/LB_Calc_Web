package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.service.LBService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lbs")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "LB",
        description = "REST API модулей хранения LB"
)
@SecurityRequirement(name = "bearerAuth")
public class LBRestController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBRestController.class);

    private final LBService lbService;

    public LBRestController(LBService lbService) {
        this.lbService = lbService;
    }

    /**
     * Получить список всех LB.
     */
    @GetMapping
    @Operation(
            summary = "Получить все LB",
            description = "Возвращает список всех модулей хранения LB"
    )
    public ResponseEntity<?> getAllLB() {

        logger.info("GET /api/v1/lbs");


            List<LBDTO> lbList = lbService.findAll();

            return ResponseEntity.ok(
                    ApiResponse.success(lbList)
            );


    }

    /**
     * Получить LB по ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить LB по ID",
            description = "Возвращает модуль хранения LB по идентификатору"
    )
    public ResponseEntity<?> getLBById(
            @Parameter(
                    description = "Идентификатор LB",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        logger.info("GET /api/v1/lbs/{}", id);


            LBDTO lb = lbService.findById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(lb)
            );


    }

    /**
     * Получить шаблон нового LB.
     * LB в БД не сохраняется.
     */
    @GetMapping("/create")
    @Operation(
            summary = "Создать шаблон LB",
            description = "Возвращает LB с параметрами по умолчанию без сохранения в БД"
    )
    public ResponseEntity<?> createLB() {

        logger.info("GET /api/v1/lbs/create");


            LBDTO lb = lbService.createLB();

            return ResponseEntity.ok(
                    ApiResponse.success(lb)
            );


    }

    /**
     * Сохранить LB.
     */
    @PostMapping
    @Operation(
            summary = "Сохранить LB",
            description = "Проверяет и сохраняет модуль хранения LB"
    )
    public ResponseEntity<?> saveLB(
            @RequestBody LBDTO dto
    ) {

        logger.info(
                "POST /api/v1/lbs: type={}",
                dto != null ? dto.getType() : null
        );


            LBDTO saved = lbService.saveLB(dto);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LB успешно сохранён",
                            saved
                    )
            );


    }
}