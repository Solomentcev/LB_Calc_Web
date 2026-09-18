package com.lb_calc_web.controller;

import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class LBCalcWebController {

    private final ALSService alsService;
    private final LCService lcService;
    private final LBService lbService;

    public LBCalcWebController(
            ALSService alsService,
            LCService lcService,
            LBService lbService
    ) {
        this.alsService = alsService;
        this.lcService = lcService;
        this.lbService = lbService;
    }

    @GetMapping
    public String init() {
        return "/myprofile";
    }

    @GetMapping("/alss")
    public String alss(
            Model model
    ) {
        model.addAttribute(
                "alss",
                alsService.findAll()
        );

        return "alss/alss";
    }

    @GetMapping("/lcs")
    public String lcs(
            Model model
    ) {
        model.addAttribute(
                "lcs",
                lcService.findAll()
        );

        return "lcs/lcs";
    }

    @GetMapping("/lbs")
    public String lbs(
            Model model
    ) {
        model.addAttribute(
                "lbs",
                lbService.findAll()
        );

        return "lbs/lbs";
    }
}