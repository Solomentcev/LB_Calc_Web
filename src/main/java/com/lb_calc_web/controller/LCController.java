package com.lb_calc_web.controller;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.attributes.BarReader;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DisplayLC;
import com.lb_calc_web.model.attributes.Payment;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lcs")
public class LCController {
    private static final Logger logger = LoggerFactory.getLogger(LCController.class);
    private final LCService lcService;
    private final List<Colors> colorsList;
    private final List<Payment> paymentList;
    private final List<DisplayLC> displayList;
    private final List<BarReader> barReaderList;


    public LCController(LCService lcService) {
        this.lcService = lcService;
        colorsList = Arrays.asList(Colors.values());
        paymentList = Arrays.asList(Payment.values());
        displayList = Arrays.asList(DisplayLC.values());
        barReaderList = Arrays.asList(BarReader.values());
    }
    @GetMapping("/create")
    private String createLC(Model model) {
        LCDTO lc = lcService.createLC();
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "/lcs/lc";
    }
    @PostMapping("/save")
    public String saveLC(@ModelAttribute("lc") LCDTO lc, Model model) {
        List<String> errorList= SizeValidator.getErrorValidateLCSizesList(lc);
        if (errorList.isEmpty()) {
            lc=lcService.saveLC(lc);
        } else {
            logger.warn(errorList.toString());
            model.addAttribute("errors",errorList);
            model.addAttribute("lc", lc);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
            return "/lcs/lc";
        }
        return "redirect:/lcs/" + lc.getId();
    }

    @GetMapping("/{id}")
    private String editLC(@PathVariable(value = "id") Long id, Model model) {
        LCDTO lc = lcService.findById(id);
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "/lcs/lc";
    }
}
