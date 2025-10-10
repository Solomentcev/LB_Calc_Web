package com.lb_calc_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class LBCalcWebController {
    public LBCalcWebController() {}
    @GetMapping
    private String init() {return "index";}

}
