package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.LBCService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


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
    @Operation(
            summary = "Получить все LBC"
    )
    public ResponseEntity<?> getAllLBC() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        lbcService.findAll()
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить LBC по ID"
    )
    public ResponseEntity<?> getLBCById(
            @Parameter(description = "Идентификатор LBC")
            @PathVariable Long id
    ) {

            return ResponseEntity.ok(
                    ApiResponse.success(
                            lbcService.findById(id)
                    )
            );

    }

    @GetMapping("/create")
    @Operation(
            summary = "Получить шаблон LBC"
    )
    public ResponseEntity<?> createLBC() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        lbcService.createLBC()
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Сохранить LBC"
    )
    public ResponseEntity<?> saveLBC(
            @RequestBody LBCDTO dto
    ) {

            LBCDTO saved =
                    lbcService.saveLBC(dto);

            return ResponseEntity.status(
                            HttpStatus.CREATED
                    )
                    .body(
                            ApiResponse.success(
                                    "LBC успешно сохранён",
                                    saved
                            )
                    );

    }
}
