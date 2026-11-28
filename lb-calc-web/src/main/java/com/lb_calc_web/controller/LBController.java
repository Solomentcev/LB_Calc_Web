package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.service.validation.size.SizeBounds;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.image.LBImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/** MVC-контроллер каталога и редактирования модулей хранения LB. */
@Controller
@RequestMapping("/lbs")
public class LBController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBController.class);

    private final LBService lbService;
    private final SizeBounds sizeBounds;

    private final List<Colors> colorsList =
            Arrays.asList(Colors.values());

    private final List<DirectionDoorOpening> directionDoorOpeningList =
            Arrays.asList(DirectionDoorOpening.values());

    public LBController(LBService lbService, SizeBounds sizeBounds) {
        this.lbService = lbService;
        this.sizeBounds = sizeBounds;
    }

    /**
     * Создание нового LB.
     */
    @GetMapping("/create")
    public String createLB(Model model) {
        model.addAttribute("pageContext", "CATALOG");

        logger.debug("Открытие страницы создания LB");

        LBDTO lb = toDtoWithImage(lbService.createLB());

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
        model.addAttribute("pageContext", "CATALOG");

        logger.debug(
                "Открытие страницы редактирования LB id={}",
                id
        );

        LBDTO lb = toDtoWithImage(lbService.findById(id));

        model.addAttribute("lb", lb);

        return "lbs/lb";
    }

    /**
     * Пересчитывает LB и обновляет форму без сохранения модуля.
     */
    @PostMapping("/refresh")
    public String refreshLB(@ModelAttribute("lb") LBDTO lb, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            lb = toDtoWithImage(LBDtoMapper.toDomain(lb, TypeLb.valueOf(lb.getType()), sizeBounds));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errors", List.of("Проверьте введённые параметры модуля."));
        }
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
        model.addAttribute("pageContext", "CATALOG");

        logger.debug(
                "Сохранение LB id={}, type={}",
                lb.getId(),
                lb.getType()
        );

        try {

            LBDTO savedLB =
                    LBDtoMapper.toDto(lbService.saveLB(LBDtoMapper.toDomain(lb, TypeLb.valueOf(lb.getType()))));

            return "redirect:/lbs/" + savedLB.getId();

        } catch (ValidationSizeException e) {

            logger.debug(
                    "LB не прошёл валидацию: {}",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "errors",
                    ValidationErrorFormatter.format(e.getValidationResult())
            );

            model.addAttribute(
                    "lb",
                    lb
            );

            return "lbs/lb";
        }
    }

    private LBDTO toDtoWithImage(LB domain) {
        LBDTO dto = LBDtoMapper.toDto(domain);
        dto.setStringLBImage(LBImageService.getStringLBImage(domain));
        return dto;
    }

    /**
     * Список доступных типов LB.
     *
     * <p>Допустимые типы определяются перечислением TypeLb.</p>
     */
    @ModelAttribute("typeLbList")
    public List<String> typeLbList() {

        return Arrays.stream(TypeLb.values()).map(Enum::name).toList();
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