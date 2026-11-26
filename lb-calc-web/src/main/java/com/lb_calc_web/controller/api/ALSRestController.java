package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.image.ALSImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST-контроллер управления конфигурациями ALS и вложенными модулями. */
@RestController
@RequestMapping("/api/v1/alss")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "ALS",
        description = "API автоматизированных систем хранения"
)
@SecurityRequirement(name = "bearerAuth")
public class ALSRestController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ALSRestController.class
            );

    private final ALSService alsService;

    public ALSRestController(
            ALSService alsService
    ) {
        this.alsService = alsService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список ALS"
    )
    /** Возвращает список конфигураций ALS. */
    public ResponseEntity<?> getAllALS() {

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
    public ResponseEntity<?> getALSById(
            @PathVariable Long id
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
    public ResponseEntity<?> createALS() {

        return ResponseEntity.ok(
                ApiResponse.success(
toDto(alsService.createALS())
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Создать ALS"
    )
    /** Создаёт конфигурацию ALS из переданных данных. */
    public ResponseEntity<?> createALS(
            @RequestBody ALSDTO dto
    ) {

            dto.setId(0L);

            ALS savedDomain = alsService.saveALS(alsService.restoreDomain(dto));
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
    public ResponseEntity<?> updateALS(
            @PathVariable Long id,
            @RequestBody ALSDTO dto
    ) {

            dto.setId(id);

            ALS savedDomain = alsService.saveALS(alsService.restoreDomain(dto));
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
    public ResponseEntity<?> deleteALS(
            @PathVariable Long id
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
    public ResponseEntity<?> getLBC(
            @PathVariable Long alsId,
            @PathVariable Long lbcId
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
    public ResponseEntity<?> updateLBC(
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            @RequestBody LBCDTO lbc
    ) {

            ALS als = alsService.findById(alsId);
            if (!(als.getControlModule() instanceof com.lb_calc_web.domain.model.LBC currentLBC)
                    || !java.util.Objects.equals(currentLBC.getId(), lbcId)) {
                throw new java.util.NoSuchElementException(
                        "LBC с id " + lbcId + " не найден в ALS " + alsId
                );
            }
            lbc.setId(lbcId);

            com.lb_calc_web.domain.model.LBC replacement =
                    alsService.restoreLBC(lbc, als);
            ALS saved = alsService.saveALS(alsService.replaceLBC(als, replacement));
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
    public ResponseEntity<?> replaceWithNewLBC(
            @PathVariable Long alsId
    ) {

            ALS saved = alsService.replaceWithNewLBCandSaveALS(alsId);
            return ResponseEntity.ok(ApiResponse.success(toDto(saved)));

    }

    /**
     * Добавить LB в ALS.
     */
    @PostMapping("/{alsId}/lb")
    @Operation(summary = "Добавить LB в ALS")
    public ResponseEntity<?> addLB(
            @PathVariable Long alsId
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
    public ResponseEntity<?> deleteLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {

            ALS saved = alsService.deleteLBandSaveALS(alsId, lbId);
            return ResponseEntity.ok(ApiResponse.success(toDto(saved)));

    }

    /**
     * Преобразует доменную ALS в DTO и формирует изображение для ответа API.
     */
    private ALSDTO toDto(com.lb_calc_web.domain.model.ALS domain) {
        ALSDTO dto = ALSDtoMapper.toDto(domain);
        dto.setStringALSImage(ALSImageService.getStringALSImage(domain));
        return dto;
    }

}