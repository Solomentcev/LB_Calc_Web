package com.lb_calc_web.controller;

import com.lb_calc_web.domain.model.LBC;
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

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/lbcs")
public class LBCController extends BaseCatalogController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBCController.class);

    private final LBCService lbcService;

    public LBCController(LBCService lbcService) {
        this.lbcService = lbcService;
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
        logger.info("Открытие страницы создания LBC");

        model.addAttribute(
                "lbc",
                LBCImageService.addImage(LBCDtoMapper.toDto(lbcService.createLBC()))
        );

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
        logger.info(
                "Открытие страницы редактирования LBC id={}",
                id
        );

        model.addAttribute(
                "lbc",
                LBCImageService.addImage(LBCDtoMapper.toDto(lbcService.findById(id)))
        );

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
        try {
            LBC saved = lbcService.saveLBC(lbcService.restoreDomain(lbc));

            return "redirect:/lbcs/" + saved.getId();

        } catch (ValidationSizeException e) {
            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );
            model.addAttribute(
                    "lbc",
                    LBCImageService.addImage(lbc)
            );

            return "lbcs/lbc";
        }
    }

    @ModelAttribute("accessMethodList")
    public List<AccessMethod> accessMethodList() {
        return Arrays.asList(AccessMethod.values());
    }

    @ModelAttribute("printOptionList")
    public List<PrintOption> printOptionList() {
        return Arrays.asList(PrintOption.values());
    }
}
