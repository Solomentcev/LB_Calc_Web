package com.lb_calc_web.controller;

import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.utils.BarReader;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DisplayLC;
import com.lb_calc_web.model.utils.Payment;
import com.lb_calc_web.service.LCImageService;
import com.lb_calc_web.service.LCService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lcs")
public class LCController {
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
    @GetMapping
    private String lcs(Model model) {
        model.addAttribute("lcs", lcService.findAll());
        return "lcs/lcs";
    }
    @GetMapping("/{id}")
    private String editLC(@PathVariable(value = "id") Long id, Model model) {
        Optional<LC> lcOptional = lcService.findById(id);
        LC lc = null;
        if (lcOptional.isPresent()) {lc = lcOptional.get();}
        else {model.addAttribute("error","LC not found");}
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        byte[] imageBytes= LCImageService.getBytesArrayLCImage(lc);
        String imageString= Base64.getEncoder().encodeToString(imageBytes);
        model.addAttribute("image", imageString);
        return "lcs/lc";
    }
    @GetMapping("/create")
    private String createLC(Model model) {
        LC lc = lcService.createLC();
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "redirect:/lcs/"+lc.getId();
    }
    @PostMapping("/save")
    public String saveLC(@ModelAttribute("lc") LC lc) {
        return "redirect:/lcs/" + lcService.save(lc).getId();
    }
}
