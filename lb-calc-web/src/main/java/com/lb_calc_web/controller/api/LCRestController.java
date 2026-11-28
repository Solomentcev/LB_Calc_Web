package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.domain.geometry.SizeBounds;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST-контроллер операций с управляющими модулями LC. */
@RestController
@Validated
@RequestMapping("/api/v1/lcs")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "LC",
        description = "REST API модулей управления LC"
)
@SecurityRequirement(name = "bearerAuth")
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Некорректный запрос или ошибка валидации",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Требуется аутентификация",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Недостаточно прав для выполнения операции",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Запрашиваемый ресурс не найден",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "405",
        description = "HTTP-метод не поддерживается",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "Конфликт текущего состояния данных",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "415",
        description = "Формат содержимого запроса не поддерживается",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
@io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "500",
        description = "Внутренняя ошибка сервера",
        content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.lb_calc_web.controller.api.response.ApiResponse.class
                )
        )
)
public class LCRestController {

    private static final Logger logger =
            LoggerFactory.getLogger(LCRestController.class);

    private final LCService lcService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;

    public LCRestController(
            LCService lcService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds
    ) {
        this.lcService = lcService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
    }

    /**
     * Получить список всех LC.
     */
    @GetMapping
    @Operation(
            summary = "Получить все LC",
            description = "Возвращает список всех модулей управления LC"
    )
    public ResponseEntity<ApiResponse<List<LCDTO>>> getAllLC() {
        logger.debug("GET /api/v1/lcs");
        List<LCDTO> lcList = LCDtoMapper.toDtoList(lcService.findAll());
        return ResponseEntity.ok(ApiResponse.success(lcList));
    }

    /**
     * Получить LC по ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить LC по ID",
            description = "Возвращает модуль управления LC по идентификатору"
    )
    public ResponseEntity<ApiResponse<LCDTO>> getLCById(
            @Parameter(description = "Идентификатор LC", example = "1")
            @PathVariable @Positive Long id
    ) {
        logger.debug("GET /api/v1/lcs/{}", id);
        LCDTO lc = LCDtoMapper.toDto(lcService.findById(id));
        return ResponseEntity.ok(ApiResponse.success(lc));
    }

    /**
     * Получить шаблон нового LC.
     */
    @GetMapping("/create")
    @Operation(
            summary = "Создать шаблон LC",
            description = "Возвращает LC с параметрами по умолчанию без сохранения в БД"
    )
    public ResponseEntity<ApiResponse<LCDTO>> createLC() {
        logger.debug("GET /api/v1/lcs/create");
        LCDTO lc = LCDtoMapper.toDto(lcService.createLC());
        return ResponseEntity.ok(ApiResponse.success(lc));
    }

    /**
     * Сохранить LC.
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Модуль LC создан")
    @Operation(
            summary = "Сохранить LC",
            description = "Проверяет и сохраняет модуль управления LC"
    )
    public ResponseEntity<ApiResponse<LCDTO>> saveLC(@RequestBody @Valid LCDTO dto) {
        logger.debug(
                "POST /api/v1/lcs: display={}",
                dto != null && dto.getFunctionalConfiguration() != null
                        ? dto.getFunctionalConfiguration().getDisplay() : null
        );
        LC domain = LCDtoMapper.toDomain(
                dto, equipmentCatalogService::findById, sizeBounds);
        lcService.prepareFunctionalEquipment(domain, dto.isEquipmentProvided());
        LCDTO saved = LCDtoMapper.toDto(lcService.saveLC(domain));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("LC успешно сохранён", saved)
        );
    }

}
