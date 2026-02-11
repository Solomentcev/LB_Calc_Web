package com.lb_calc_web.controller;

import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.TypeLb;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lbs")
public class LBController {
    private static final Logger logger = LoggerFactory.getLogger(LBController.class);
    private final LBService lbService;
    private final List<TypeLb> typeLbList;
    private final List<Colors> colorsList;
    private final List<DirectionDoorOpening> directionDoorOpeningList;

    public LBController(LBService lbService) {
        this.lbService = lbService;
        typeLbList= Arrays.asList(TypeLb.values());
        colorsList = Arrays.asList(Colors.values());
        directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());
    }
    @GetMapping("/create")
    private String createLB( Model model) {
        LBDTO lb =lbService.createLB();
        model.addAttribute("lb", lb);
        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);

        return "/lbs/lb";
    }
    @GetMapping("/{id}")
    private String editLB(@PathVariable(value = "id") Long id, Model model){
            Optional<LBDTO> lbOptional = lbService.findById(id);
            if (lbOptional.isPresent()) {
                LBDTO lb = lbOptional.get();
                model.addAttribute("lb", lb);
                model.addAttribute("typeLbList", typeLbList);
                model.addAttribute("colorsList", colorsList);
                model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
            }
        return "lbs/lb";
    }
    @PostMapping("/save")
    public String saveLB(@ModelAttribute("lb") LBDTO lb, Model model){
          logger.debug(String.valueOf(lb));
          List<String> errorList= SizeValidator.getErrorValidateLBSizesList(lb);
        if (errorList.isEmpty()) {
            lb=lbService.saveLB(lb);
        } else {
            logger.warn(errorList.toString());
            model.addAttribute("errors",errorList);
            model.addAttribute("lb", lb);
            model.addAttribute("typeLbList", typeLbList);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
            return "lbs/lb";
        }
        return "redirect:/lbs/" +lb.getId();
    }



}
