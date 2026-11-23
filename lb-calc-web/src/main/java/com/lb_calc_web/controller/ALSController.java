package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.image.ALSImageService;
import com.lb_calc_web.service.image.LBCImageService;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.ALSService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/alss")
public class ALSController extends BaseCatalogController {

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
        ALSDTO als = ALSImageService.addImages(ALSDtoMapper.toDto(alsService.createALS()));

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
        ALSDTO als = ALSImageService.addImages(ALSDtoMapper.toDto(alsService.findById(id)));

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
            // Главная форма ALS редактирует параметры агрегата, но не состав LC/LBC.
            // Восстанавливаем управляющий модуль из БД, чтобы форма не теряла
            // каталожное оборудование, которое не редактируется на этой странице.
            if (als.getId() != null && als.getId() > 0) {
                ALSDTO persisted = ALSDtoMapper.toDto(alsService.findById(als.getId()));
                als.setLC(persisted.getLC());
                als.setLBC(persisted.getLBC());
            }

            com.lb_calc_web.domain.model.ALS domain = alsService.restoreDomain(als);
            com.lb_calc_web.domain.model.ALS saved = alsService.saveALS(domain);

            return "redirect:/alss/"
                    + saved.getId();

        } catch (ValidationSizeException e) {

            model.addAttribute(
                    "ALSErrors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "als",
                    ALSImageService.addImages(als)
            );

            return "alss/als";
        }
    }

    @PostMapping("/{alsId}/addLB")
    public String addLB(
            @PathVariable Long alsId
    ) {
        ALSDTO als =
                ALSDtoMapper.toDto(alsService.addNewLBandSaveALS(
                        alsId
                ));

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
        com.lb_calc_web.domain.model.ALS domain = alsService.findById(alsId);
        ALSDTO als = ALSImageService.addImages(ALSDtoMapper.toDto(domain));

        model.addAttribute("als", als);
        model.addAttribute(
                "lbc",
                LBCImageService.addImage(LBCDtoMapper.toDto(alsService.findLBCInALS(domain, lbcId)))
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
            com.lb_calc_web.domain.model.ALS als =
                    alsService.findById(alsId);
            lbc.setId(lbcId);

            com.lb_calc_web.domain.model.LBC replacement =
                    alsService.restoreLBC(lbc, als);
            com.lb_calc_web.domain.model.ALS saved =
                    alsService.saveALS(alsService.replaceLBC(als, replacement));

            return "redirect:/alss/"
                    + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(alsService.findById(alsId))));
            model.addAttribute("lbc", LBCImageService.addImage(lbc));

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
        com.lb_calc_web.domain.model.ALS saved =
                alsService.replaceWithNewLBCandSaveALS(alsId);

        return "redirect:/alss/"
                + saved.getId();
    }

    @PostMapping("/{alsId}/lbs/{lbId}/delete")
    public String deleteLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {
        ALSDTO als =
                ALSDtoMapper.toDto(alsService.deleteLBandSaveALS(
                        alsId,
                        lbId
                ));

        return "redirect:/alss/"
                + als.getId();
    }
}
