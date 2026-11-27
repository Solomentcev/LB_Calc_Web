package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.service.LBService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST-контроллер операций с модулями хранения LB. */
@RestController
@Validated
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
    private final Environment environment;

    public LBRestController(LBService lbService, Environment environment) {
        this.lbService = lbService;
        this.environment = environment;
    }

    /**
     * Получить список всех LB.
     */
    @GetMapping
    @Operation(
            summary = "Получить все LB",
            description = "Возвращает список всех модулей хранения LB"
    )
    public ResponseEntity<ApiResponse<List<LBDTO>>> getAllLB() {

        logger.debug("GET /api/v1/lbs");

            List<LBDTO> lbList = lbService.findAll().stream().map(LBDtoMapper::toDto).toList();

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
    public ResponseEntity<ApiResponse<LBDTO>> getLBById(
            @Parameter(
                    description = "Идентификатор LB",
                    example = "1"
            )
            @PathVariable @Positive Long id
    ) {

        logger.debug("GET /api/v1/lbs/{}", id);

            LBDTO lb = LBDtoMapper.toDto(lbService.findById(id));

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
    public ResponseEntity<ApiResponse<LBDTO>> createLB() {

        logger.debug("GET /api/v1/lbs/create");

            LBDTO lb = LBDtoMapper.toDto(lbService.createLB());

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
    public ResponseEntity<ApiResponse<LBDTO>> saveLB(
            @RequestBody @Valid LBDTO dto
    ) {

        logger.debug(
                "POST /api/v1/lbs: type={}",
                dto != null ? dto.getType() : null
        );

            LBDTO saved = LBDtoMapper.toDto(lbService.saveLB(LBDtoMapper.toDomain(dto, TypeLbFactory.forLB(environment, dto.getType()))));

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(
                            "LB успешно сохранён",
                            saved
                    )
            );

    }
}