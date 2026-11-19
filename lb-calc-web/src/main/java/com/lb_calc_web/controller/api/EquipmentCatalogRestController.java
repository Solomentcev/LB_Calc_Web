package com.lb_calc_web.controller.api;

import com.lb_calc_web.dto.EquipmentCatalogDTO;
import com.lb_calc_web.entity.EquipmentCatalogEntity;
import com.lb_calc_web.service.EquipmentCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST API администрирования каталога оборудования. */
@RestController
@RequestMapping("/api/v1/equipment-catalog")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class EquipmentCatalogRestController {
    private final EquipmentCatalogService service;

    public EquipmentCatalogRestController(EquipmentCatalogService service) {
        this.service = service;
    }

    @GetMapping
    public List<EquipmentCatalogDTO> findAll(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<EquipmentCatalogEntity> items = activeOnly
                ? service.findActive() : service.findAll();
        return items.stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public EquipmentCatalogDTO get(@PathVariable String id) {
        return toDto(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentCatalogDTO create(@RequestBody EquipmentCatalogDTO dto) {
        return toDto(service.create(toEntity(dto)));
    }

    @PutMapping("/{id}")
    public EquipmentCatalogDTO update(@PathVariable String id,
                                      @RequestBody EquipmentCatalogDTO dto) {
        return toDto(service.update(id, dto.category(), dto.name(), dto.description(),
                dto.width(), dto.height(), dto.depth(), dto.displayWidth(),
                dto.displayHeight(), dto.minimumModuleWidth(), dto.supportsBarcode1d(),
                dto.supportsBarcode2d(), dto.supportsRfid(), dto.supportsPrint()));
    }

    @PatchMapping("/{id}/active")
    public EquipmentCatalogDTO setActive(@PathVariable String id,
                                         @RequestParam boolean active) {
        return toDto(service.setActive(id, active));
    }

    @PatchMapping("/{id}/default")
    public EquipmentCatalogDTO setDefault(@PathVariable String id,
                                          @RequestParam boolean defaultPosition) {
        return toDto(service.setDefault(id, defaultPosition));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private EquipmentCatalogDTO toDto(EquipmentCatalogEntity item) {
        return new EquipmentCatalogDTO(item.getCatalogPositionId(), item.getCategory(),
                item.getName(), item.getDescription(), item.getWidth(), item.getHeight(),
                item.getDepth(), item.getDisplayWidth(), item.getDisplayHeight(),
                item.getMinimumModuleWidth(), item.isSupportsBarcode1d(),
                item.isSupportsBarcode2d(), item.isSupportsRfid(), item.isSupportsPrint(),
                item.isActive(), item.isDefaultPosition());
    }

    private EquipmentCatalogEntity toEntity(EquipmentCatalogDTO dto) {
        EquipmentCatalogEntity entity = new EquipmentCatalogEntity(
                dto.catalogPositionId(), dto.category(), dto.name(), dto.description(),
                dto.width(), dto.height(), dto.depth(), dto.displayWidth(),
                dto.displayHeight(), dto.minimumModuleWidth(), dto.supportsBarcode1d(),
                dto.supportsBarcode2d(), dto.supportsRfid(), dto.supportsPrint(), dto.active());
        entity.setDefaultPosition(dto.defaultPosition());
        return entity;
    }
}
