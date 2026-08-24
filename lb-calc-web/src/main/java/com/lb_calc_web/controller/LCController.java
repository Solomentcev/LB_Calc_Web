package com.lb_calc_web.controller;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.attributes.*;
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
    protected final List<Colors> colorsList = Arrays.asList(Colors.values());
    protected final List<Payment> paymentList = Arrays.asList(Payment.values());
    protected final List<DisplayLC> displayList = Arrays.asList(DisplayLC.values());
    protected final List<BarReader> barReaderList = Arrays.asList(BarReader.values());

    public LCController(LCService lcService) {
        this.lcService = lcService;

    }
    @GetMapping("/create")
    private String createLC(Model model) {
        LCDTO lc = lcService.createLC();
        model.addAttribute("lc", lc);

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

            return "/lcs/lc";
        }
        return "redirect:/lcs/" + lc.getId();
    }

    @GetMapping("/{id}")
    private String editLC(@PathVariable(value = "id") Long id, Model model) {
        LCDTO lc = lcService.findById(id);
        model.addAttribute("lc", lc);

        return "/lcs/lc";
    }
    @ModelAttribute("colorsList")
    public List<Colors> colorsList() { return colorsList; }

    @ModelAttribute("paymentList")
    public List<Payment> paymentList() { return paymentList; }

    @ModelAttribute("displayList")
    public List<DisplayLC> displayList() { return displayList; }

    @ModelAttribute("barReaderList")
    public List<BarReader> barReaderList() { return barReaderList; }
}
