package com.lb_calc_web.controller.api;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.ControlModule;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.validation.size.SizeBounds;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.lb_calc_web.service.image.ALSImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST-контроллер управления конфигурациями ALS и вложенными модулями. */
@RestController
@Validated
@RequestMapping("/api/v1/alss")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "ALS",
        description = "API автоматизированных систем хранения"
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
public class ALSRestController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ALSRestController.class
            );

    private final ALSService alsService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;

    public ALSRestController(
            ALSService alsService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds
    ) {
        this.alsService = alsService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
    }

    @GetMapping
    @Operation(
            summary = "Получить список ALS"
    )
    /** Возвращает список конфигураций ALS. */
    public ResponseEntity<ApiResponse<List<ALSDTO>>> getAllALS() {

        List<ALSDTO> alsList =
                alsService.findAll().stream().map(this::toDto).toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ALS list fetched successfully",
                        alsList
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить ALS по ID"
    )
    /** Возвращает конфигурацию ALS по идентификатору. */
    public ResponseEntity<ApiResponse<ALSDTO>> getALSById(
            @PathVariable @Positive Long id
    ) {

            ALSDTO als = toDto(alsService.findById(id));

            return ResponseEntity.ok(
                    ApiResponse.success(als)
            );

    }

    @GetMapping("/create")
    @Operation(
            summary = "Получить шаблон ALS"
    )
    /** Создаёт конфигурацию ALS из переданных данных. */
    public ResponseEntity<ApiResponse<ALSDTO>> createALS() {

        return ResponseEntity.ok(
                ApiResponse.success(
toDto(alsService.createALS())
                )
        );
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "ALS создана")
    @Operation(
            summary = "Создать ALS"
    )
    /** Создаёт конфигурацию ALS из переданных данных. */
    public ResponseEntity<ApiResponse<ALSDTO>> createALS(
            @RequestBody @Valid ALSDTO dto
    ) {

            dto.setId(0L);

            ALS restored = ALSDtoMapper.toDomain(
                    dto,
                    candidate -> TypeLb.valueOf(candidate.getType()),
                    candidate -> TypeLb.valueOf(candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            restored = alsService.prepareConfiguration(null, restored, dto.isEquipmentSelectionProvided());
            ALS savedDomain = alsService.saveALS(restored);
            ALSDTO saved = toDto(savedDomain);

            return ResponseEntity.status(
                            HttpStatus.CREATED
                    )
                    .body(
                            ApiResponse.success(
                                    saved
                            )
                    );

    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить ALS"
    )
    /** Обновляет сохранённую конфигурацию ALS. */
    public ResponseEntity<ApiResponse<ALSDTO>> updateALS(
            @PathVariable @Positive Long id,
            @RequestBody @Valid ALSDTO dto
    ) {

            dto.setId(id);

            ALS existing = alsService.findById(id);
            ALS restored = ALSDtoMapper.toDomain(
                    dto,
                    candidate -> TypeLb.valueOf(candidate.getType()),
                    candidate -> TypeLb.valueOf(candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            restored = alsService.prepareConfiguration(existing, restored, dto.isEquipmentSelectionProvided());
            ALS savedDomain = alsService.saveALS(restored);
            ALSDTO saved = toDto(savedDomain);

            return ResponseEntity.ok(
                    ApiResponse.success(saved)
            );

    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить ALS"
    )
    /** Удаляет конфигурацию ALS по идентификатору. */
    public ResponseEntity<ApiResponse<Void>> deleteALS(
            @PathVariable @Positive Long id
    ) {

            alsService.deleteALS(id);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "ALS deleted successfully"
                    )
            );

    }

    /**
     * Получить LBC, входящий в ALS.
     */
    @GetMapping("/{alsId}/lbc/{lbcId}")
    @Operation(summary = "Получить LBC ALS")
    public ResponseEntity<ApiResponse<LBCDTO>> getLBC(
            @PathVariable @Positive Long alsId,
            @PathVariable @Positive Long lbcId
    ) {

            ALS als = alsService.findById(alsId);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            LBCDtoMapper.toDto(alsService.findLBCInALS(als, lbcId))
                    )
            );

    }

    /**
     * Обновить LBC внутри ALS.
     */
    @PutMapping("/{alsId}/lbc/{lbcId}")
    @Operation(summary = "Обновить LBC ALS")
    public ResponseEntity<ApiResponse<LBCDTO>> updateLBC(
            @PathVariable @Positive Long alsId,
            @PathVariable @Positive Long lbcId,
            @RequestBody @Valid LBCDTO lbc
    ) {

            ALS als = alsService.findById(alsId);
            if (!(als.getControlModule() instanceof com.lb_calc_web.domain.model.LBC currentLBC)
                    || !java.util.Objects.equals(currentLBC.getId(), lbcId)) {
                throw new java.util.NoSuchElementException(
                        "LBC с id " + lbcId + " не найден в ALS " + alsId
                );
            }
            lbc.setId(lbcId);

            LBC replacement = LBCDtoMapper.toDomain(
                    lbc,
                    TypeLb.valueOf(lbc.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            ALS saved = alsService.replaceLBCandSaveALS(als, replacement, lbc.isEquipmentProvided());
            com.lb_calc_web.domain.model.LBC savedLBC =
                    (com.lb_calc_web.domain.model.LBC) saved.getControlModule();
            return ResponseEntity.ok(
                    ApiResponse.success(
                            com.lb_calc_web.mapper.dto.LBCDtoMapper.toDto(savedLBC)
                    )
            );

    }

    /**
     * Заменить управляющий модуль ALS новым LBC.
     */
    @PostMapping("/{alsId}/replace-lbc")
    @Operation(summary = "Заменить управляющий модуль ALS на новый LBC")
    public ResponseEntity<ApiResponse<ALSDTO>> replaceWithNewLBC(
            @PathVariable @Positive Long alsId
    ) {

            ALS saved = alsService.replaceWithNewLBCandSaveALS(alsId);
            return ResponseEntity.ok(ApiResponse.success(toDto(saved)));

    }

    /**
     * Добавить LB в ALS.
     */
    @PostMapping("/{alsId}/lb")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Модуль LB добавлен")
    @Operation(summary = "Добавить LB в ALS")
    public ResponseEntity<ApiResponse<ALSDTO>> addLB(
            @PathVariable @Positive Long alsId
    ) {

            ALSDTO saved = toDto(alsService.addNewLBandSaveALS(alsId));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(saved));

    }

    /**
     * Удалить LB из ALS.
     */
    @DeleteMapping("/{alsId}/lb/{lbId}")
    @Operation(summary = "Удалить LB из ALS")
    public ResponseEntity<ApiResponse<ALSDTO>> deleteLB(
            @PathVariable @Positive Long alsId,
            @PathVariable @Positive Long lbId
    ) {

            ALS saved = alsService.deleteLBandSaveALS(alsId, lbId);
            return ResponseEntity.ok(ApiResponse.success(toDto(saved)));

    }

    /** Преобразует доменную ALS в DTO и формирует изображение для ответа API. */
    private ALSDTO toDto(com.lb_calc_web.domain.model.ALS domain) {
        ALSDTO dto = ALSDtoMapper.toDto(domain);
        dto.setStringALSImage(ALSImageService.getStringALSImage(domain));
        return dto;
    }

}