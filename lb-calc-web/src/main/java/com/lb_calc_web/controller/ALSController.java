package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.ALSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/alss")
public class ALSController extends BaseCatalogController {

    private static final Logger logger =
            LoggerFactory.getLogger(ALSController.class);

    private final ALSService alsService;

    public ALSController(
            ALSService alsService
    ) {
        this.alsService = alsService;
    }

    @GetMapping("/create")
    public String createALS(
            Model model
    ) {
        ALSDTO als =
                alsService.createALS();

        model.addAttribute(
                "als",
                als
        );

        return "alss/als";
    }

    @GetMapping("/{id}")
    public String editALS(
            @PathVariable Long id,
            Model model
    ) {
        ALSDTO als =
                alsService.findById(id);

        model.addAttribute(
                "als",
                als
        );

        return "alss/als";
    }

    @PostMapping("/save")
    public String saveALS(
            @ModelAttribute("als") ALSDTO als,
            Model model
    ) {
        try {
            ALSDTO saved =
                    alsService.saveALS(als);

            return "redirect:/alss/"
                    + saved.getId();

        } catch (ValidationSizeException e) {

            logger.warn(
                    "Ошибка валидации ALS: {}",
                    e.getErrors()
            );

            model.addAttribute(
                    "ALSErrors",
                    e.getErrors()
            );

            model.addAttribute(
                    "als",
                    als
            );

            return "alss/als";
        }
    }

    @PostMapping("/{alsId}/addLB")
    public String addLB(
            @PathVariable Long alsId
    ) {
        ALSDTO als =
                alsService.addNewLBandSaveALS(
                        alsId
                );

        return "redirect:/alss/"
                + als.getId();
    }

    /**
     * Форма редактирования LBC внутри ALS.
     */
    @GetMapping("/{alsId}/lbcs/{lbcId}")
    public String editLBC(
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            Model model
    ) {
        ALSDTO als = alsService.findById(alsId);

        model.addAttribute("als", als);
        model.addAttribute(
                "lbc",
                als.getLBC()
        );

        return "alss/alss_lbc";
    }

    /**
     * Сохранение LBC внутри ALS.
     */
    @PostMapping("/{alsId}/lbcs/{lbcId}/save")
    public String saveLBC(
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            @ModelAttribute("lbc") com.lb_calc_web.dto.LBCDTO lbc,
            Model model
    ) {
        try {
            ALSDTO als =
                    alsService.findById(alsId);

            lbc.setId(lbcId);

            ALSDTO saved =
                    alsService.replaceLBCandSaveALS(
                            als,
                            lbc
                    );

            return "redirect:/alss/"
                    + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", e.getErrors());
            model.addAttribute("als", alsService.findById(alsId));
            model.addAttribute("lbc", lbc);

            return "alss/alss_lbc";
        }
    }

    /**
     * Заменяет текущий control-модуль на новый LBC.
     */
    @PostMapping("/{alsId}/replaceLBC")
    public String replaceLBC(
            @PathVariable Long alsId
    ) {
        ALSDTO saved =
                alsService.replaceWithNewLBCandSaveALS(
                        alsId
                );

        return "redirect:/alss/"
                + saved.getId();
    }

    @PostMapping("/{alsId}/lbs/{lbId}/delete")
    public String deleteLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {
        ALSDTO als =
                alsService.deleteLBandSaveALS(
                        alsId,
                        lbId
                );

        return "redirect:/alss/"
                + als.getId();
    }
}