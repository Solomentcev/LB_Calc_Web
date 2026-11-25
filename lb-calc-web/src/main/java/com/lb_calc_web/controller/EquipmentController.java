package com.lb_calc_web.controller;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.equipment.SystemPurpose;
import com.lb_calc_web.dto.EquipmentDTO;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogFilter;
import com.lb_calc_web.service.EquipmentCatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/equipment")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
/** MVC-контроллер каталога физических моделей оборудования. */
public class EquipmentController {

    private final EquipmentCatalogService equipmentCatalogService;

    public EquipmentController(EquipmentCatalogService equipmentCatalogService) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) EquipmentFunctionalType functionalType,
            @RequestParam(required = false) EquipmentCategory category,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) Integer widthMin,
            @RequestParam(required = false) Integer widthMax,
            @RequestParam(required = false) Integer heightMin,
            @RequestParam(required = false) Integer heightMax,
            @RequestParam(required = false) Integer depthMin,
            @RequestParam(required = false) Integer depthMax,
            @RequestParam(required = false) String description,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "functionalType", "category", "purpose",
                        "width", "height", "depth", "description"));
        EquipmentCatalogFilter filter = new EquipmentCatalogFilter(
                id, name, functionalType, category, purpose,
                widthMin, widthMax, heightMin, heightMax, depthMin, depthMax, description);
        var result = equipmentCatalogService.findPage(filter, pageable)
                .map(EquipmentDtoMapper::toDto);
        model.addAttribute("equipmentList", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("sort", pageable.getSort().stream()
                .findFirst().map(order -> order.getProperty()).orElse("id"));
        model.addAttribute("direction", pageable.getSort().stream()
                .findFirst().map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("filters", filter);
        model.addAttribute("functionalTypes", Arrays.asList(EquipmentFunctionalType.values()));
        model.addAttribute("categories", Arrays.asList(EquipmentCategory.values()));
        return "equipment/equipments";
    }

    @GetMapping("/create")
    /** Открывает форму создания модели оборудования. */
    public String createForm(Model model) {
        prepareForm(model, new EquipmentDTO());
        return "equipment/equipment";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        prepareForm(model, EquipmentDtoMapper.toDto(equipmentCatalogService.findById(id)));
        return "equipment/equipment";
    }

    @PostMapping("/save")
    /** Проверяет и сохраняет модель оборудования. */
    public String save(
            @Valid @ModelAttribute("equipment") EquipmentDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, dto);
            return "equipment/equipment";
        }

        try {
            Equipment saved = equipmentCatalogService.save(
                    EquipmentDtoMapper.toDomain(dto, dto.getId()));
            redirectAttributes.addFlashAttribute("successMessage", "Модель оборудования сохранена");
            return "redirect:/equipment/" + saved.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            prepareForm(model, dto);
            return "equipment/equipment";
        }
    }

    @PostMapping("/{id}/delete")
    /** Удаляет модель оборудования из каталога. */
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            equipmentCatalogService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Модель оборудования удалена");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/equipment";
    }

    private void prepareForm(Model model, EquipmentDTO dto) {
        model.addAttribute("equipment", dto);
        model.addAttribute("functionalTypes", Arrays.asList(EquipmentFunctionalType.values()));
        model.addAttribute("categories", Arrays.asList(EquipmentCategory.values()));

        List<String> purposes = new ArrayList<>();
        Arrays.stream(DisplayPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(AccessPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(PaymentPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(PrinterPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(SystemPurpose.values()).map(Enum::name).forEach(purposes::add);
        model.addAttribute("purposes", purposes.stream().distinct().toList());
    }
}
