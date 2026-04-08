package com.lb_calc_web.controller;

import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.TypeLb;
import com.lb_calc_web.service.LBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/lbs")
public class LBController {
    private static final Logger logger = LoggerFactory.getLogger(LBController.class);
    private final LBService lbService;
    protected final List<Colors> colorsList = Arrays.asList(Colors.values());
    protected final List<TypeLb> typeLbList = Arrays.asList(TypeLb.values());
    protected final List<DirectionDoorOpening> directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());

    public LBController(LBService lbService) {
        this.lbService = lbService;

    }
    @GetMapping("/create")
    private String createLB( Model model) {
        LBDTO lb =lbService.createLB();
        model.addAttribute("lb", lb);

        return "/lbs/lb";
    }
    @GetMapping("/{id}")
    private String editLB(@PathVariable(value = "id") Long id, Model model){
                LBDTO lb =lbService.findById(id);
                model.addAttribute("lb", lb);
        return "lbs/lb";
    }
    @PostMapping("/save")
    public String saveLB(@ModelAttribute("lb") LBDTO lb, Model model){
        try {
            lb=lbService.saveLB(lb);
        } catch (ValidationSizeException e) {
            model.addAttribute("errors",e.getErrors());
            model.addAttribute("lb", lb);
            return "lbs/lb";
        }

        return "redirect:/lbs/" +lb.getId();
    }
    @ModelAttribute("typeList")
    public List<TypeLb> typeList() { return typeLbList; }
    @ModelAttribute("typeLbList")
    public List<TypeLb> typeLbList() { return typeLbList; }
    @ModelAttribute("directionDoorOpeningList")
    public List<DirectionDoorOpening> directionDoorOpeningList() { return directionDoorOpeningList; }
    @ModelAttribute("colorsList")
    public List<Colors> colorsList() { return colorsList; }


}
