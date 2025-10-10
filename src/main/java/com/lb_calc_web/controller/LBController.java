package com.lb_calc_web.controller;

import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DirectionDoorOpening;
import com.lb_calc_web.model.utils.TypeLb;
import com.lb_calc_web.service.LBService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lbs")
public class LBController {
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
    @GetMapping
    private String lbs(Model model) {
        model.addAttribute("lbs", lbService.findAll());
        return "lbs/lbs";
    }
    @GetMapping("/{id}")
    private String editLB(@PathVariable(value = "id") Long id, Model model) {

        Optional<LB> lbOptional = lbService.findById(id);
        LB lb = null;
        if (lbOptional.isPresent()) {lb = lbOptional.get();}
        else {model.addAttribute("error","LB not found");}
        model.addAttribute("lb", lb);
        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);

        return "lbs/lb";
    }
    @PostMapping("/save")
    public String saveLB(@ModelAttribute("lb") LB lb) {
        return "redirect:/lbs/" + lbService.save(lb).getId();
    }
    @GetMapping("/create")
    private String createLB( Model model) {
        List<TypeLb> typeLbList= Arrays.asList(TypeLb.values());
        List<Colors> colorsList = Arrays.asList(Colors.values());
        List<DirectionDoorOpening> directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());
        LB lb =lbService.createLB();
        model.addAttribute("lb", lb);
        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);

        return "redirect:/lbs/"+lb.getId();
    }
}
