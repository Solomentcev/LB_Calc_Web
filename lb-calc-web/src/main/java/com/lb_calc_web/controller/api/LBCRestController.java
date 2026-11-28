package com.lb_calc_web.controller.api;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.LBCService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.validation.size.SizeBounds;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/lbcs")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "LBC",
        description = "REST API комбинированных модулей LBC"
)
@SecurityRequirement(name = "bearerAuth")
/** REST-контроллер операций с комбинированными модулями LBC. */
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
public class LBCRestController {

    private final LBCService lbcService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;

    public LBCRestController(
            LBCService lbcService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds
    ) {
        this.lbcService = lbcService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
    }

    @GetMapping
    @Operation(summary = "Получить все LBC")
    public ResponseEntity<ApiResponse<List<LBCDTO>>> getAllLBC() {
        List<LBCDTO> modules = LBCDtoMapper.toDtoList(lbcService.findAll());
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить LBC по ID")
    public ResponseEntity<ApiResponse<LBCDTO>> getLBCById(
            @Parameter(description = "Идентификатор LBC")
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(LBCDtoMapper.toDto(lbcService.findById(id)))
        );
    }

    @GetMapping("/create")
    @Operation(summary = "Получить шаблон LBC")
    public ResponseEntity<ApiResponse<LBCDTO>> createLBC() {
        return ResponseEntity.ok(
                ApiResponse.success(LBCDtoMapper.toDto(lbcService.createLBC()))
        );
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Модуль LBC создан")
    @Operation(summary = "Сохранить LBC")
    public ResponseEntity<ApiResponse<LBCDTO>> saveLBC(@RequestBody @Valid LBCDTO dto) {
        LBC domain = LBCDtoMapper.toDomain(
                dto,
                TypeLb.valueOf(dto.getType()),
                equipmentCatalogService::findById, sizeBounds
        );
        lbcService.prepareFunctionalEquipment(domain, dto.isEquipmentProvided());
        LBCDTO saved = LBCDtoMapper.toDto(lbcService.saveLBC(domain));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("LBC успешно сохранён", saved));
    }

}
