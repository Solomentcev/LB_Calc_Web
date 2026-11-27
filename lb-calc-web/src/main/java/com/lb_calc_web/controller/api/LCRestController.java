package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.validation.size.SizeBounds;
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

/** REST-контроллер операций с управляющими модулями LC. */
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

    /** Преобразует DTO LC в доменную модель с учётом сохранённой комплектации. */
    private LC toDomain(LCDTO dto) {
        ControlConfiguration fallback = dto.getId() != null && dto.getId() > 0
                ? lcService.findById(dto.getId()).getControlConfiguration() : null;
        LC restored = LCDtoMapper.toDomain(
                dto, equipmentCatalogService::findById, fallback, sizeBounds);
        lcService.configureFunctionalEquipment(restored);
        return restored;
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
    public ResponseEntity<?> getLCById(
            @Parameter(description = "Идентификатор LC", example = "1")
            @PathVariable Long id
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
    public ResponseEntity<?> createLC() {
        logger.debug("GET /api/v1/lcs/create");
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
        logger.debug(
                "POST /api/v1/lcs: display={}",
                dto != null && dto.getFunctionalConfiguration() != null
                        ? dto.getFunctionalConfiguration().getDisplay() : null
        );
        LC domain = toDomain(dto);
        LCDTO saved = LCDtoMapper.toDto(lcService.saveLC(domain));
        return ResponseEntity.ok(
                ApiResponse.success("LC успешно сохранён", saved)
        );
    }

}
