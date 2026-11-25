package com.lb_calc_web.controller;

import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.dto.EquipmentDTO;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.image.LBCImageService;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.LBCService;
import com.lb_calc_web.service.ModuleCatalogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/** MVC-контроллер каталога и редактирования комбинированных модулей LBC. */
@Controller
@RequestMapping("/lbcs")
public class LBCController extends BaseCatalogController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBCController.class);

    private final LBCService lbcService;
    private final EquipmentCatalogService equipmentCatalogService;

    public LBCController(LBCService lbcService, EquipmentCatalogService equipmentCatalogService) {
        this.lbcService = lbcService;
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /**
     * Список всех LBC.
     */
    @GetMapping
    public String lbcs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @ModelAttribute ModuleCatalogFilter filter,
            Model model
    ) {
        Set<String> allowedSorts = Set.of("id", "name", "type", "countCells", "height", "width",
                "depth", "displayPurpose", "accessSort", "paymentPurpose", "printerPurpose",
                "colorBody", "colorDoor");
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id", allowedSorts);
        var result = lbcService.findPage(filter, pageable).map(
                LBCDtoMapper::toDto).map(LBCImageService::addImage);
        model.addAttribute("lbcs", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("filters", filter);
        model.addAttribute("sort", allowedSorts.contains(sort) ? sort : "id");
        model.addAttribute("direction", pageable.getSort().stream().findFirst()
                .map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
        return "lbcs/lbcs";
    }

    /**
     * Создание нового LBC.
     */
    @GetMapping("/create")
    public String createLBC(Model model) {
        model.addAttribute("pageContext", "CATALOG");
        logger.debug("Открытие страницы создания LBC");

        prepareForm(model, LBCImageService.addImage(LBCDtoMapper.toDto(lbcService.createLBC())));

        return "lbcs/lbc";
    }

    /**
     * Редактирование существующего LBC.
     */
    @GetMapping("/{id}")
    public String editLBC(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("pageContext", "CATALOG");
        logger.debug(
                "Открытие страницы редактирования LBC id={}",
                id
        );

        prepareForm(model, LBCImageService.addImage(LBCDtoMapper.toDto(lbcService.findById(id))));

        return "lbcs/lbc";
    }

    /** Обновляет комплектацию LBC без сохранения модуля. */
    @PostMapping("/refresh")
    public String refreshLBC(@ModelAttribute("lbc") LBCDTO lbc, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            lbc = LBCDtoMapper.toDto(lbcService.restoreDomain(lbc));
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        }
        prepareForm(model, LBCImageService.addImage(lbc));
        return "lbcs/lbc";
    }

    /**
     * Сохранение LBC.
     */
    @PostMapping("/save")
    public String saveLBC(
            @ModelAttribute("lbc") LBCDTO lbc,
            Model model
    ) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            equipmentCatalogService.validateSelections(lbc.getFunctionalConfiguration(), lbc.getEquipmentSelections());
            LBC saved = lbcService.saveLBC(lbcService.restoreDomain(lbc));

            return "redirect:/lbcs/" + saved.getId();

        } catch (ValidationSizeException e) {
            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );
            prepareForm(model, LBCImageService.addImage(lbc));

            return "lbcs/lbc";
        }
    }

    private void prepareForm(Model model, LBCDTO lbc) {
        model.addAttribute("lbc", lbc);
        lbc.setEquipmentSelections(equipmentCatalogService.prepareSelections(
                lbc.getFunctionalConfiguration(), lbc.getEquipmentSelections()));
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }
}
