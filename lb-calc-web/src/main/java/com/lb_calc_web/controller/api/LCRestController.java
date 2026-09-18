package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.LCService;
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

        try {
            List<LCDTO> lcList =
                    lcService.findAll();

            return ResponseEntity.ok(
                    ApiResponse.success(lcList)
            );

        } catch (Exception e) {

            logger.error(
                    "Ошибка при получении списка LC",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при получении списка LC",
                                    e.getMessage()
                            )
                    );
        }
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

        try {
            LCDTO lc =
                    lcService.findById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(lc)
            );

        } catch (NoSuchElementException e) {

            logger.warn(
                    "LC с id={} не найден",
                    id
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            ApiResponse.error(
                                    "LC с id=" + id + " не найден"
                            )
                    );

        } catch (Exception e) {

            logger.error(
                    "Ошибка при получении LC id={}",
                    id,
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при получении LC",
                                    e.getMessage()
                            )
                    );
        }
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

        try {
            LCDTO lc =
                    lcService.createLC();

            return ResponseEntity.ok(
                    ApiResponse.success(lc)
            );

        } catch (Exception e) {

            logger.error(
                    "Ошибка при создании шаблона LC",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при создании шаблона LC",
                                    e.getMessage()
                            )
                    );
        }
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

        try {

            LCDTO saved =
                    lcService.saveLC(dto);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "LC успешно сохранён",
                            saved
                    )
            );

        } catch (ValidationSizeException e) {

            logger.warn(
                    "LC не прошёл валидацию: {}",
                    e.getErrors()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.error(
                                    "LC не прошёл валидацию",
                                    e.getErrors()
                            )
                    );

        } catch (IllegalArgumentException e) {

            logger.warn(
                    "Некорректные параметры LC: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );

        } catch (IllegalStateException e) {

            /*
             * Например:
             * ширина LC меньше минимальной ширины оборудования.
             */
            logger.warn(
                    "Некорректное состояние LC: {}",
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
                    "Ошибка при сохранении LC",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ApiResponse.error(
                                    "Ошибка при сохранении LC",
                                    e.getMessage()
                            )
                    );
        }
    }
}