package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.handler.ValidationSizeException;
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
public class LBCController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBCController.class);

    private final LBCService lbcService;

    public LBCController(LBCService lbcService) {
        this.lbcService = lbcService;
    }

    @GetMapping("/create")
    public String createLBC(Model model) {
        logger.info("Открытие страницы создания LBC");

        model.addAttribute(
                "lbc",
                lbcService.createLBC()
        );

        return "lbcs/lbc";
    }

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
                lbcService.findById(id)
        );

        return "lbcs/lbc";
    }

    @PostMapping("/save")
    public String saveLBC(
            @ModelAttribute("lbc") LBCDTO lbc,
            Model model
    ) {
        try {
            LBCDTO saved = lbcService.saveLBC(lbc);

            return "redirect:/lbcs/" + saved.getId();

        } catch (ValidationSizeException e) {
            model.addAttribute("errors", e.getErrors());
            model.addAttribute("lbc", lbc);

            return "lbcs/lbc";
        }
    }

    @ModelAttribute("colorsList")
    public List<Colors> colorsList() {
        return Arrays.asList(Colors.values());
    }

    @ModelAttribute("paymentList")
    public List<Payment> paymentList() {
        return Arrays.asList(Payment.values());
    }

    @ModelAttribute("accessMethodList")
    public List<AccessMethod> accessMethodList() {
        return Arrays.asList(AccessMethod.values());
    }

    @ModelAttribute("printOptionList")
    public List<PrintOption> printOptionList() {
        return Arrays.asList(PrintOption.values());
    }

    @ModelAttribute("displayList")
    public List<String> displayList() {
        return List.of(
                "Без дисплея",
                "LC10",
                "LC17",
                "LC19"
        );
    }

    @ModelAttribute("barReaderList")
    public List<String> barReaderList() {
        return List.of(
                "Без сканера",
                "Сканер штрихкода 1D",
                "Сканер штрихкода 2D"
        );
    }

    @ModelAttribute("typeLbList")
    public List<String> typeLbList() {
        String types =
                lbcServiceTypeList();

        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(type -> !type.isBlank())
                .toList();
    }

    @ModelAttribute("typeList")
    public List<String> typeList() {
        return typeLbList();
    }

    @ModelAttribute("directionDoorOpeningList")
    public List<DirectionDoorOpening> directionDoorOpeningList() {
        return Arrays.asList(DirectionDoorOpening.values());
    }

    private String lbcServiceTypeList() {
        return org.springframework.core.env.AbstractEnvironment
                .ACTIVE_PROFILES_PROPERTY_NAME
                .equals("never")
                ? ""
                : "";
    }
}
