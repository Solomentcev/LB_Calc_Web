package com.lb_calc_web.controller.api;

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
    public List<EquipmentCatalogEntity> findAll(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly ? service.findActive() : service.findAll();
    }

    @GetMapping("/{id}")
    public EquipmentCatalogEntity get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentCatalogEntity create(@RequestBody EquipmentCatalogEntity item) {
        return service.create(item);
    }

    @PatchMapping("/{id}/active")
    public EquipmentCatalogEntity setActive(@PathVariable String id,
                                             @RequestParam boolean active) {
        return service.setActive(id, active);
    }
}
