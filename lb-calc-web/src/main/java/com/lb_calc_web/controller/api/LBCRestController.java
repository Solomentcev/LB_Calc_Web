package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.LBCService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.domain.geometry.SizeBounds;
import org.springframework.core.env.Environment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lbcs")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "LBC",
        description = "REST API комбинированных модулей LBC"
)
@SecurityRequirement(name = "bearerAuth")
/** REST-контроллер операций с комбинированными модулями LBC. */
public class LBCRestController {

    private final LBCService lbcService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;
    private final Environment environment;

    public LBCRestController(
            LBCService lbcService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds,
            Environment environment
    ) {
        this.lbcService = lbcService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
        this.environment = environment;
    }

    @GetMapping
    @Operation(summary = "Получить все LBC")
    public ResponseEntity<?> getAllLBC() {
        List<LBCDTO> modules = LBCDtoMapper.toDtoList(lbcService.findAll());
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить LBC по ID")
    public ResponseEntity<?> getLBCById(
            @Parameter(description = "Идентификатор LBC")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(LBCDtoMapper.toDto(lbcService.findById(id)))
        );
    }

    @GetMapping("/create")
    @Operation(summary = "Получить шаблон LBC")
    public ResponseEntity<?> createLBC() {
        return ResponseEntity.ok(
                ApiResponse.success(LBCDtoMapper.toDto(lbcService.createLBC()))
        );
    }

    @PostMapping
    @Operation(summary = "Сохранить LBC")
    public ResponseEntity<?> saveLBC(@RequestBody LBCDTO dto) {
        LBC domain = LBCDtoMapper.toDomain(
                dto,
                TypeLbFactory.forLBC(environment, dto.getType()),
                equipmentCatalogService::findById, sizeBounds
        );
        lbcService.prepareFunctionalEquipment(domain, dto.isEquipmentProvided());
        LBCDTO saved = LBCDtoMapper.toDto(lbcService.saveLBC(domain));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("LBC успешно сохранён", saved));
    }

}
