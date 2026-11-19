package com.lb_calc_web.controller;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.LCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/lcs")
public class LCController extends BaseCatalogController {

    private static final Logger logger =
            LoggerFactory.getLogger(LCController.class);

    private final LCService lcService;

    public LCController(LCService lcService, com.lb_calc_web.service.EquipmentCatalogService equipmentCatalogService) {
        super(equipmentCatalogService);
        this.lcService = lcService;
    }

    /**
     * Создание нового LC.
     */
    @GetMapping("/create")
    public String createLC(Model model) {

        logger.info("Открытие страницы создания LC");

        LCDTO lc = lcService.createLC();

        model.addAttribute("lc", lc);

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

        logger.info(
                "Открытие страницы редактирования LC id={}",
                id
        );

        LCDTO lc = lcService.findById(id);

        model.addAttribute("lc", lc);

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

        logger.info(
                "Сохранение LC id={}, display={}",
                lc.getId(),
                lc.getDisplay()
        );

        try {

            LCDTO savedLC =
                    lcService.saveLC(lc);

            return "redirect:/lcs/" + savedLC.getId();

        } catch (ValidationSizeException e) {

            logger.warn(
                    "LC не прошёл валидацию: {}",
                    e.getErrors()
            );

            model.addAttribute(
                    "errors",
                    e.getErrors()
            );

            model.addAttribute(
                    "lc",
                    lc
            );

            return "lcs/lc";
        }
    }

}