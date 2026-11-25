package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.image.LCImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/lcs")
public class LCController {

    private static final Logger logger =
            LoggerFactory.getLogger(LCController.class);

    private final LCService lcService;

    private final List<Colors> colorsList =
            Arrays.asList(Colors.values());

    private final EquipmentCatalogService equipmentCatalogService;

    public LCController(LCService lcService, EquipmentCatalogService equipmentCatalogService) {
        this.lcService = lcService;
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /**
     * Создание нового LC.
     */
    @GetMapping("/create")
    public String createLC(Model model) {
        model.addAttribute("pageContext", "CATALOG");

        logger.info("Открытие страницы создания LC");

        LCDTO lc = LCImageService.addImage(LCDtoMapper.toDto(lcService.createLC()));
        lc.setId(0L);

        prepareForm(model, lc);

        return "lcs/lc";
    }

    /**
     * Редактирование существующего LC.
     */
    @GetMapping("/{id}")
    public String editLC(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("pageContext", "CATALOG");

        logger.info(
                "Открытие страницы редактирования LC id={}",
                id
        );

        LCDTO lc = LCImageService.addImage(LCDtoMapper.toDto(lcService.findById(id)));

        prepareForm(model, lc);

        return "lcs/lc";
    }

    /**
     * Сохранение LC.
     */
    @PostMapping("/save")
    public String saveLC(
            @ModelAttribute("lc") LCDTO lc,
            Model model
    ) {
        model.addAttribute("pageContext", "CATALOG");

        logger.info(
                "Сохранение LC id={}, display={}",
                lc.getId(),
                lc.getFunctionalConfiguration()
        );

        try {

            LC savedLC = lcService.saveLC(lcService.restoreDomain(lc));

            return "redirect:/lcs/" + savedLC.getId();

        } catch (ValidationSizeException e) {

            logger.warn(
                    "LC не прошёл валидацию: {}",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            prepareForm(model, LCImageService.addImage(lc));

            return "lcs/lc";
        }
    }

    /**
     * Список цветов.
     */
    @ModelAttribute("colorsList")
    public List<Colors> colorsList() {
        return colorsList;
    }

    private void prepareForm(Model model, LCDTO lc) {
        model.addAttribute("lc", lc);
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }
}
