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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public String lbcs(Model model) {
        model.addAttribute(
                "lbcs",
                lbcService.findAll().stream().map(LBCDtoMapper::toDto).map(LBCImageService::addImage).toList()
        );

        return "lbcs/lbcs";
    }

    /**
     * Создание нового LBC.
     */
    @GetMapping("/create")
    public String createLBC(Model model) {
        model.addAttribute("pageContext", "CATALOG");
        logger.info("Открытие страницы создания LBC");

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
        logger.info(
                "Открытие страницы редактирования LBC id={}",
                id
        );

        prepareForm(model, LBCImageService.addImage(LBCDtoMapper.toDto(lbcService.findById(id))));

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
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }
}
