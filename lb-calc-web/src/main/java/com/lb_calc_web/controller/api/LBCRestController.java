package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.handler.ValidationSizeException;
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
import java.util.NoSuchElementException;

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
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            lbcService.findById(id)
                    )
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(
                            HttpStatus.NOT_FOUND
                    )
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
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
        try {
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
        } catch (ValidationSizeException e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    "LBC не прошёл валидацию",
                                    e.getErrors()
                            )
                    );
        } catch (IllegalArgumentException
                 | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.error(
                                    e.getMessage()
                            )
                    );
        }
    }
}
