package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.LBService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

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

        try {
            List<LBDTO> lbList = lbService.findAll();

            return ResponseEntity.ok(
                    ApiResponse.success(lbList)
            );

        } catch (Exception e) {
            logger.error(
                    "Ошибка при получении списка LB",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при получении списка LB",
                                    e.getMessage()
                            )
                    );
        }
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

        try {
            LBDTO lb = lbService.findById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(lb)
            );

        } catch (NoSuchElementException e) {
            logger.warn(
                    "LB с id={} не найден",
                    id
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "LB с id=" + id + " не найден"
                            )
                    );

        } catch (Exception e) {
            logger.error(
                    "Ошибка при получении LB id={}",
                    id,
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при получении LB",
                                    e.getMessage()
                            )
                    );
        }
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

        try {
            LBDTO lb = lbService.createLB();

            return ResponseEntity.ok(
                    ApiResponse.success(lb)
            );

        } catch (Exception e) {
            logger.error(
                    "Ошибка при создании шаблона LB",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при создании шаблона LB",
                                    e.getMessage()
                            )
                    );
        }
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

        try {
            LBDTO saved = lbService.saveLB(dto);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LB успешно сохранён",
                            saved
                    )
            );

        } catch (ValidationSizeException e) {
            logger.warn(
                    "Ошибка валидации LB: {}",
                    e.getErrors()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.error(
                                    "LB не прошёл валидацию",
                                    e.getErrors()
                            )
                    );

        } catch (IllegalArgumentException e) {
            logger.warn(
                    "Некорректные параметры LB: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {
            logger.error(
                    "Ошибка при сохранении LB",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при сохранении LB",
                                    e.getMessage()
                            )
                    );
        }
    }
}