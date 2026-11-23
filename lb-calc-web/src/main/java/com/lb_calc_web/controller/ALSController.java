package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.image.ALSImageService;
import com.lb_calc_web.service.image.LBImageService;
import com.lb_calc_web.service.image.LCImageService;
import com.lb_calc_web.service.image.LBCImageService;
import com.lb_calc_web.service.validation.ValidationSizeException;
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
        ALSDTO als = toDto(alsService.createALS());

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
                toDto(alsService.findById(id));

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
                ALSDTO persisted = toDto(alsService.findById(als.getId()));
                als.setLC(persisted.getLC());
                als.setLBC(persisted.getLBC());
            }

            com.lb_calc_web.domain.model.ALS domain = alsService.restoreDomain(als);
            com.lb_calc_web.domain.model.ALS saved = alsService.saveALS(domain);

            return "redirect:/alss/"
                    + saved.getId();

        } catch (ValidationSizeException e) {

            logger.warn(
                    "Ошибка валидации ALS: {}",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "ALSErrors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "als",
                    toDto(alsService.findById(als.getId()))
            );

            return "alss/als";
        }
    }

    @PostMapping("/{alsId}/addLB")
    public String addLB(
            @PathVariable Long alsId
    ) {
        ALSDTO als =
                toDto(alsService.addNewLBandSaveALS(
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
        ALSDTO als = toDto(domain);

        model.addAttribute("als", als);
        model.addAttribute(
                "lbc",
                addLBCImage(LBCDtoMapper.toDto(alsService.findLBCInALS(domain, lbcId)))
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
            model.addAttribute("als", toDto(alsService.findById(alsId)));
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
                toDto(alsService.deleteLBandSaveALS(
                        alsId,
                        lbId
                ));

        return "redirect:/alss/"
                + als.getId();
    }
    /**
     * Преобразует доменную ALS в модель представления и добавляет изображение.
     */
    private ALSDTO toDto(com.lb_calc_web.domain.model.ALS als) {
        ALSDTO dto = ALSDtoMapper.toDto(als);
        try {
            dto.setStringALSImage(ALSImageService.getStringALSImage(dto));
        } catch (RuntimeException exception) {
            logger.warn("Не удалось сформировать изображение ALS id={}: {}", dto.getId(), exception.getMessage());
        }
        if (dto.getLC() != null) {
            try {
                dto.getLC().setStringLCImage(LCImageService.getStringLCImage(dto.getLC()));
            } catch (RuntimeException exception) {
                logger.warn("Не удалось сформировать изображение LC id={}: {}", dto.getLC().getId(), exception.getMessage());
            }
        }
        if (dto.getLBC() != null) addLBCImage(dto.getLBC());
        if (dto.getLbList() != null) {
            dto.getLbList().forEach(lb -> {
                try {
                    lb.setStringLBImage(LBImageService.getStringLBImage(lb));
                } catch (RuntimeException exception) {
                    logger.warn("Не удалось сформировать изображение LB id={}: {}", lb.getId(), exception.getMessage());
                }
            });
        }
        return dto;
    }

    private void addLBCImage(com.lb_calc_web.dto.LBCDTO lbc) {
        try {
            lbc.setStringLBCImage(LBCImageService.getStringLBCImage(lbc));
        } catch (RuntimeException exception) {
            logger.warn("Не удалось сформировать изображение LBC id={}: {}", lbc.getId(), exception.getMessage());
        }
    }

}