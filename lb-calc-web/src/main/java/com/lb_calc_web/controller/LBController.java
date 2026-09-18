package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.LBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/lbs")
public class LBController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBController.class);

    private final LBService lbService;
    private final Environment environment;

    private final List<Colors> colorsList =
            Arrays.asList(Colors.values());

    private final List<DirectionDoorOpening> directionDoorOpeningList =
            Arrays.asList(DirectionDoorOpening.values());

    public LBController(
            LBService lbService,
            Environment environment
    ) {
        this.lbService = lbService;
        this.environment = environment;
    }

    /**
     * Создание нового LB.
     */
    @GetMapping("/create")
    public String createLB(Model model) {

        logger.info("Открытие страницы создания LB");

        LBDTO lb = lbService.createLB();

        model.addAttribute("lb", lb);

        return "lbs/lb";
    }

    /**
     * Редактирование существующего LB.
     */
    @GetMapping("/{id}")
    public String editLB(
            @PathVariable Long id,
            Model model
    ) {

        logger.info(
                "Открытие страницы редактирования LB id={}",
                id
        );

        LBDTO lb = lbService.findById(id);

        model.addAttribute("lb", lb);

        return "lbs/lb";
    }

    /**
     * Сохранение LB.
     */
    @PostMapping("/save")
    public String saveLB(
            @ModelAttribute("lb") LBDTO lb,
            Model model
    ) {

        logger.info(
                "Сохранение LB id={}, type={}",
                lb.getId(),
                lb.getType()
        );

        try {

            LBDTO savedLB =
                    lbService.saveLB(lb);

            return "redirect:/lbs/" + savedLB.getId();

        } catch (ValidationSizeException e) {

            logger.warn(
                    "LB не прошёл валидацию: {}",
                    e.getErrors()
            );

            model.addAttribute(
                    "errors",
                    e.getErrors()
            );

            model.addAttribute(
                    "lb",
                    lb
            );

            return "lbs/lb";
        }
    }

    /**
     * Список доступных типов LB.
     *
     * <p>Типы не являются Java enum.
     * Они задаются в size-bounds.properties.</p>
     */
    @ModelAttribute("typeLbList")
    public List<String> typeLbList() {

        String types =
                environment.getRequiredProperty(
                        "lb.types"
                );

        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(type -> !type.isBlank())
                .toList();
    }

    /**
     * Совместимость с текущим шаблоном.
     */
    @ModelAttribute("typeList")
    public List<String> typeList() {
        return typeLbList();
    }

    /**
     * Цвета корпуса и дверей.
     */
    @ModelAttribute("colorsList")
    public List<Colors> colorsList() {
        return colorsList;
    }

    /**
     * Направления открытия дверей.
     */
    @ModelAttribute("directionDoorOpeningList")
    public List<DirectionDoorOpening> directionDoorOpeningList() {
        return directionDoorOpeningList;
    }
}