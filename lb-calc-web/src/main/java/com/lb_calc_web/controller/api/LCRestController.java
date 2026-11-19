package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.service.LCService;
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
@RequestMapping("/api/v1/lcs")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "LC",
        description = "REST API модулей управления LC"
)
@SecurityRequirement(name = "bearerAuth")
public class LCRestController {

    private static final Logger logger =
            LoggerFactory.getLogger(LCRestController.class);

    private final LCService lcService;

    public LCRestController(LCService lcService) {
        this.lcService = lcService;
    }

    /**
     * Получить список всех LC.
     */
    @GetMapping
    @Operation(
            summary = "Получить все LC",
            description = "Возвращает список всех модулей управления LC"
    )
    public ResponseEntity<?> getAllLC() {

        logger.info("GET /api/v1/lcs");

            List<LCDTO> lcList =
                    lcService.findAll();

            return ResponseEntity.ok(
                    ApiResponse.success(lcList)
            );

    }

    /**
     * Получить LC по ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить LC по ID",
            description = "Возвращает модуль управления LC по идентификатору"
    )
    public ResponseEntity<?> getLCById(
            @Parameter(
                    description = "Идентификатор LC",
                    example = "1"
            )
            @PathVariable Long id
    ) {

        logger.info(
                "GET /api/v1/lcs/{}",
                id
        );

            LCDTO lc =
                    lcService.findById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(lc)
            );

    }

    /**
     * Получить шаблон нового LC.
     */
    @GetMapping("/create")
    @Operation(
            summary = "Создать шаблон LC",
            description = "Возвращает LC с параметрами по умолчанию без сохранения в БД"
    )
    public ResponseEntity<?> createLC() {

        logger.info(
                "GET /api/v1/lcs/create"
        );

            LCDTO lc =
                    lcService.createLC();

            return ResponseEntity.ok(
                    ApiResponse.success(lc)
            );

    }

    /**
     * Сохранить LC.
     */
    @PostMapping
    @Operation(
            summary = "Сохранить LC",
            description = "Проверяет и сохраняет модуль управления LC"
    )
    public ResponseEntity<?> saveLC(
            @RequestBody LCDTO dto
    ) {

        logger.info(
                "POST /api/v1/lcs: display={}",
                dto != null ? dto.getDisplay() : null
        );


            LCDTO saved =
                    lcService.saveLC(dto);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LC успешно сохранён",
                            saved
                    )
            );

    }
}