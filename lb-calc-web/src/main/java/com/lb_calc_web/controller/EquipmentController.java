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

/** MVC-контроллер каталога физических моделей оборудования. */
@Controller
@RequestMapping("/equipment")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class EquipmentController {

    private final EquipmentCatalogService equipmentCatalogService;

    public EquipmentController(EquipmentCatalogService equipmentCatalogService) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /** Отображает каталог моделей оборудования с фильтрацией, сортировкой и пагинацией. */
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

    /** Открывает форму создания модели оборудования. */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("equipment", new EquipmentDTO());
        model.addAttribute("functionalTypes", Arrays.asList(EquipmentFunctionalType.values()));
        model.addAttribute("categories", Arrays.asList(EquipmentCategory.values()));
        model.addAttribute("purposes", equipmentPurposes());
        return "equipment/equipment";
    }

    /** Открывает форму редактирования выбранной модели оборудования. */
    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("equipment", EquipmentDtoMapper.toDto(equipmentCatalogService.findById(id)));
        model.addAttribute("functionalTypes", Arrays.asList(EquipmentFunctionalType.values()));
        model.addAttribute("categories", Arrays.asList(EquipmentCategory.values()));
        model.addAttribute("purposes", equipmentPurposes());
        return "equipment/equipment";
    }

    /** Проверяет и сохраняет модель оборудования. */
    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("equipment") EquipmentDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("equipment", dto);
            model.addAttribute("functionalTypes", Arrays.asList(EquipmentFunctionalType.values()));
        model.addAttribute("categories", Arrays.asList(EquipmentCategory.values()));
        model.addAttribute("purposes", equipmentPurposes());
            return "equipment/equipment";
        }

        try {
            Equipment saved = equipmentCatalogService.save(
                    EquipmentDtoMapper.toDomain(dto, dto.getId()));
            redirectAttributes.addFlashAttribute("successMessage", "Модель оборудования сохранена");
            return "redirect:/equipment/" + saved.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("equipment", dto);
            model.addAttribute("functionalTypes", Arrays.asList(EquipmentFunctionalType.values()));
        model.addAttribute("categories", Arrays.asList(EquipmentCategory.values()));
        model.addAttribute("purposes", equipmentPurposes());
            return "equipment/equipment";
        }
    }

    /** Удаляет модель оборудования из каталога. */
    @PostMapping("/{id}/delete")
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

    private static List<String> equipmentPurposes() {
        List<String> purposes = new ArrayList<>();
        Arrays.stream(DisplayPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(AccessPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(PaymentPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(PrinterPurpose.values()).map(Enum::name).forEach(purposes::add);
        Arrays.stream(SystemPurpose.values()).map(Enum::name).forEach(purposes::add);
        return purposes.stream().distinct().toList();
    }
}
