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
import com.lb_calc_web.service.EquipmentCatalogService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/equipment")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class EquipmentController {

    private final EquipmentCatalogService equipmentCatalogService;

    public EquipmentController(EquipmentCatalogService equipmentCatalogService) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "functionalType", "category", "purpose", "width", "height", "depth"));
        var result = equipmentCatalogService.findPage(q, pageable).map(EquipmentDtoMapper::toDto);
        model.addAttribute("equipmentList", result.getContent());
        CatalogPageSupport.addModelAttributes(model, result, q, pageable, "/equipment",
                CatalogPageSupport.sortOptions("id", "ID", "name", "Название",
                        "functionalType", "Функциональный тип", "category", "Категория",
                        "purpose", "Назначение", "width", "Ширина", "height", "Высота",
                        "depth", "Глубина"));
        return "equipment/equipments";
    }

    @GetMapping("/create")
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
