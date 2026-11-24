package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.LBCService;
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
public class LBCRestController {

    private final LBCService lbcService;

    public LBCRestController(LBCService lbcService) {
        this.lbcService = lbcService;
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
        LBC domain = lbcService.restoreDomain(dto);
        LBCDTO saved = LBCDtoMapper.toDto(lbcService.saveLBC(domain));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("LBC успешно сохранён", saved));
    }

}
