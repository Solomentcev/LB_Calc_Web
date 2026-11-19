package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.ALSService;
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
    public ResponseEntity<?> getAllALS() {

        List<ALSDTO> alsList =
                alsService.findAll();

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
    public ResponseEntity<?> getALSById(
            @PathVariable Long id
    ) {


            ALSDTO als =
                    alsService.findById(id);

            return ResponseEntity.ok(
                    ApiResponse.success(als)
            );


    }

    @GetMapping("/create")
    @Operation(
            summary = "Получить шаблон ALS"
    )
    public ResponseEntity<?> createALS() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        alsService.createALS()
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Создать ALS"
    )
    public ResponseEntity<?> createALS(
            @RequestBody ALSDTO dto
    ) {


            dto.setId(0L);

            ALSDTO saved =
                    alsService.saveALS(dto);

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
    public ResponseEntity<?> updateALS(
            @PathVariable Long id,
            @RequestBody ALSDTO dto
    ) {


            dto.setId(id);

            ALSDTO saved =
                    alsService.saveALS(dto);

            return ResponseEntity.ok(
                    ApiResponse.success(saved)
            );


    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить ALS"
    )
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

            ALSDTO als = alsService.findById(alsId);
            return ResponseEntity.ok(
                    ApiResponse.success(alsService.findLBCInALS(als, lbcId))
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

            ALSDTO als = alsService.findById(alsId);
            lbc.setId(lbcId);

            ALSDTO saved = alsService.replaceLBCandSaveALS(als, lbc);
            return ResponseEntity.ok(
                    ApiResponse.success(saved.getLBC())
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

            ALSDTO saved = alsService.replaceWithNewLBCandSaveALS(alsId);
            return ResponseEntity.ok(ApiResponse.success(saved));

    }

    /**
     * Добавить LB в ALS.
     */
    @PostMapping("/{alsId}/lb")
    @Operation(summary = "Добавить LB в ALS")
    public ResponseEntity<?> addLB(
            @PathVariable Long alsId
    ) {

            ALSDTO saved = alsService.addNewLBandSaveALS(alsId);
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

            ALSDTO saved = alsService.deleteLBandSaveALS(alsId, lbId);
            return ResponseEntity.ok(ApiResponse.success(saved));

    }

}