package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.dto.EquipmentDTO;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST API административного управления каталогом оборудования. */
@RestController
@RequestMapping("/api/v1/equipment")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Equipment", description = "Управление каталогом физических моделей оборудования")
@SecurityRequirement(name = "bearerAuth")
/** REST-контроллер операций чтения и управления каталогом моделей оборудования. */
public class EquipmentRestController {

    private final EquipmentCatalogService equipmentCatalogService;

    public EquipmentRestController(EquipmentCatalogService equipmentCatalogService) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /** Возвращает список моделей оборудования. */
    @GetMapping
    @Operation(summary = "Получить каталог оборудования")
    public ResponseEntity<ApiResponse<List<EquipmentDTO>>> findAll() {
        List<EquipmentDTO> result = equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Возвращает модель оборудования по идентификатору. */
    @GetMapping("/{id}")
    @Operation(summary = "Получить модель оборудования по ID")
    public ResponseEntity<ApiResponse<EquipmentDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                EquipmentDtoMapper.toDto(equipmentCatalogService.findById(id))));
    }

    /** Создаёт модель оборудования. */
    @PostMapping
    @Operation(summary = "Добавить модель оборудования")
    public ResponseEntity<ApiResponse<EquipmentDTO>> create(@Valid @RequestBody EquipmentDTO dto) {
        Equipment saved = equipmentCatalogService.save(EquipmentDtoMapper.toDomain(dto, null));
        return ResponseEntity.ok(ApiResponse.success(
                "Модель оборудования создана", EquipmentDtoMapper.toDto(saved)));
    }

    /** Обновляет существующую модель оборудования. */
    @PutMapping("/{id}")
    @Operation(summary = "Изменить модель оборудования")
    public ResponseEntity<ApiResponse<EquipmentDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentDTO dto
    ) {
        Equipment saved = equipmentCatalogService.save(EquipmentDtoMapper.toDomain(dto, id));
        return ResponseEntity.ok(ApiResponse.success(
                "Модель оборудования обновлена", EquipmentDtoMapper.toDto(saved)));
    }

    /** Удаляет модель оборудования по идентификатору. */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить модель оборудования")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        equipmentCatalogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Модель оборудования удалена"));
    }
}
