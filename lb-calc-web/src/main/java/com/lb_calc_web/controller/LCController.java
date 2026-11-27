package com.lb_calc_web.controller;

import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.image.LCImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lb_calc_web.service.validation.size.SizeBounds;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/** MVC-контроллер каталога и редактирования управляющих модулей LC. */
@Controller
@RequestMapping("/lcs")
public class LCController extends BaseCatalogController {

    private static final Logger logger =
            LoggerFactory.getLogger(LCController.class);

    private final LCService lcService;

    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;

    public LCController(
            LCService lcService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds
    ) {
        this.lcService = lcService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
    }

    /**
     * Создание нового LC.
     */
    @GetMapping("/create")
    public String createLC(Model model) {
        model.addAttribute("pageContext", "CATALOG");

        logger.debug("Открытие страницы создания LC");

        LC domain = lcService.createLC();
        LCDTO lc = toDtoWithImage(domain);
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

        logger.debug(
                "Открытие страницы редактирования LC id={}",
                id
        );

        LCDTO lc = toDtoWithImage(lcService.findById(id));

        prepareForm(model, lc);

        return "lcs/lc";
    }

    /** Обновляет комплектацию LC без сохранения модуля. */
    @PostMapping("/refresh")
    public String refreshLC(@ModelAttribute("lc") LCDTO lc, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            LC domain = toDomain(lc);
            lc = toDtoWithImage(domain);
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            model.addAttribute("errors", List.of("Проверьте введённые параметры модуля."));
        }
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

        logger.debug(
                "Сохранение LC id={}, display={}",
                lc.getId(),
                lc.getFunctionalConfiguration()
        );

        try {
            LC domain = lcService.restoreDomain(lc);
            LC savedLC = lcService.saveLC(domain);

            return "redirect:/lcs/" + savedLC.getId();

        } catch (ValidationSizeException e) {

            logger.debug(
                    "LC не прошёл валидацию: {}",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            prepareForm(model, lc);

            return "lcs/lc";
        } catch (IllegalArgumentException | NoSuchElementException e) {
            model.addAttribute("errors", List.of(e.getMessage()));
            prepareForm(model, lc);
            return "lcs/lc";
        }
    }

    /** Восстанавливает доменную модель LC из данных формы. */
    private LC toDomain(LCDTO dto) {
        ControlConfiguration fallback = dto.getId() != null && dto.getId() > 0
                ? lcService.findById(dto.getId()).getControlConfiguration()
                : null;
        LC domain = LCDtoMapper.toDomain(
                dto, equipmentCatalogService::findById, fallback, sizeBounds);
        lcService.configureFunctionalEquipment(domain);
        return domain;
    }

    private LCDTO toDtoWithImage(LC domain) {
        LCDTO dto = LCDtoMapper.toDto(domain);
        dto.setStringLCImage(LCImageService.getStringLCImage(domain));
        return dto;
    }

    private void prepareForm(Model model, LCDTO lc) {
        model.addAttribute("lc", lc);
        var equipmentCatalog = equipmentCatalogService.findAll();
        model.addAttribute("equipmentList", equipmentCatalog.stream()
                .map(EquipmentDtoMapper::toDto).toList());
    }
}
