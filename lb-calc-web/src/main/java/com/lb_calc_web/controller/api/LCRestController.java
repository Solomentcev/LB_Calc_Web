package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
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
import java.util.Set;
import java.util.function.Function;

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
    private final EquipmentCatalogService equipmentCatalogService;

    public LCRestController(
            LCService lcService,
            EquipmentCatalogService equipmentCatalogService
    ) {
        this.lcService = lcService;
        this.equipmentCatalogService = equipmentCatalogService;
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
    public ResponseEntity<?> getLCById(
            @Parameter(description = "Идентификатор LC", example = "1")
            @PathVariable Long id
    ) {
        logger.info("GET /api/v1/lcs/{}", id);
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
    public ResponseEntity<?> createLC() {
        logger.info("GET /api/v1/lcs/create");
        LCDTO lc = LCDtoMapper.toDto(lcService.createLC());
        return ResponseEntity.ok(ApiResponse.success(lc));
    }

    /**
     * Сохранить LC.
     */
    @PostMapping
    @Operation(
            summary = "Сохранить LC",
            description = "Проверяет и сохраняет модуль управления LC"
    )
    public ResponseEntity<?> saveLC(@RequestBody LCDTO dto) {
        logger.info(
                "POST /api/v1/lcs: display={}",
                dto != null ? dto.getDisplay() : null
        );
        LC domain = restoreDomain(dto);
        LCDTO saved = LCDtoMapper.toDto(lcService.saveLC(domain));
        return ResponseEntity.ok(
                ApiResponse.success("LC успешно сохранён", saved)
        );
    }

    /**
     * Восстанавливает входную конфигурацию на границе REST API.
     */
    private LC restoreDomain(LCDTO dto) {
        LC existing = dto.getId() == null || dto.getId() <= 0
                ? null
                : lcService.findById(dto.getId());
        Set<String> retainedIds = existing == null
                ? Set.of()
                : existing.getEquipment().stream()
                        .map(item -> item.getCatalogPositionId())
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Function<String, Equipment> resolver = existing == null
                ? equipmentCatalogService::resolve
                : equipmentCatalogService.findExistingConfigurationResolver(retainedIds);

        List<EquipmentSelectionDTO> selections = dto.getEquipmentSelections();
        if (selections == null && existing != null) {
            selections = EquipmentSelectionMapper.mergeDisplaySelection(
                    LCDtoMapper.toDto(existing).getEquipmentSelections(),
                    dto.getDisplay(),
                    dto.getDisplayQuantity(),
                    resolver
            );
        }
        return LCDtoMapper.toDomain(dto, selections, resolver);
    }
}