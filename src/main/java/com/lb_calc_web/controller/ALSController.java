package com.lb_calc_web.controller;

import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.utils.*;
import com.lb_calc_web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/alss")
public class ALSController {
    private final ALSService alsService;
    private final LCService lcService;
    private final LBService lbService;
    private final List<Colors> colorsList;
    private final List<PositionLC> positionLCList;
    private final List<Payment> paymentList;
    private final List<DisplayLC> displayList;
    private final List<BarReader> barReaderList;
    private final List<TypeLb> typeLbList;
    private final List<DirectionDoorOpening> directionDoorOpeningList;
    @Autowired
    public ALSController(ALSService alsService, LCService lcService, LBService lbService) {
        this.alsService = alsService;
        this.lcService = lcService;
        this.lbService = lbService;
        colorsList = Arrays.asList(Colors.values());
        positionLCList = Arrays.asList(PositionLC.values());
        paymentList = Arrays.asList(Payment.values());
        displayList = Arrays.asList(DisplayLC.values());
        barReaderList = Arrays.asList(BarReader.values());
        typeLbList= Arrays.asList(TypeLb.values());
        directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());
    }
    @GetMapping
    private String alss(Model model) {
        model.addAttribute("alss", alsService.findAll());
        return "alss/alss";
    }

    @GetMapping("/{id}")
    private String editALS(@PathVariable(value = "id") Long id, Model model) {
        Optional<ALS> alsOptional = alsService.findById(id);
        ALS als = null;
        if (alsOptional.isPresent()) {
            als = alsOptional.get();
        } else {
            model.addAttribute("error", "ALS not found");
        }
        model.addAttribute("als", als);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        byte[] imageBytes= ALSImageService.getBytesArrayALSImage(als);
        String imageString= Base64.getEncoder().encodeToString(imageBytes);
        model.addAttribute("image", imageString);
        return "alss/als";
    }

    @PostMapping("/save")
    public String saveALS(@ModelAttribute("als") ALS als) {
        als = alsService.save(als);
        return "redirect:/alss/" + als.getId();
    }

    @GetMapping("/create")
    private String createALS(Model model) {
        ALS als = alsService.createALS();
        model.addAttribute("als", als);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        return "redirect:/alss/" + als.getId();
    }

    @GetMapping("/{alsId}/addLB")
    public String addLB(@PathVariable(value = "alsId") Long alsId,
                        Model model) {
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        als = alsService.addLB(als);
        model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId();
    }

    @GetMapping("{alsId}/lbs/{lbId}/delete")
    public String deleteLB(@PathVariable(value = "alsId") Long alsId,
                           @PathVariable(value = "lbId") Long lbId,
                           Model model) {
        ALS als=alsService.deleteLB(alsId, lbId);
        model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId();
    }
    @GetMapping("/{alsId}/lcs/{lcId}")
    private String editLC(@PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lcId") Long lcId,
                          Model model) {

        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("als", als);
        Optional<LC> lcOptional = lcService.findById(lcId);
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
        return "alss/alss_lc";
    }
    @PostMapping("{alsId}/lcs/{lcId}/save")
    public String updateLC(
                           @PathVariable(value = "alsId") Long alsId,
                           @PathVariable(value = "lcId") Long lcId,
                           @ModelAttribute("lc") LC lc) {
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        als=alsService.updateLC(als,lc);
        return "redirect:/alss/" + als.getId()+"/lcs/"+als.getLc().getId();
    }
    @GetMapping("/{alsId}/lbs/{lbId}")
    private String editLB(@PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lbId") Long lbId,
                          Model model) {

        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("als", als);
        Optional<LB> lbOptional = lbService.findById(lbId);
        LB lb = null;
        if (lbOptional.isPresent()) {lb = lbOptional.get();}
        else {model.addAttribute("error","LB not found");}
        model.addAttribute("lb", lb);
        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
        byte[] imageBytes= LBImageService.getBytesArrayLBImage(lb);
        String imageString= Base64.getEncoder().encodeToString(imageBytes);
        model.addAttribute("image", imageString);
        System.out.println("ALScontroller/editLB "+lb);
        return "alss/alss_lb";
    }
    @PostMapping("{alsId}/lbs/{lbId}/save")
    public String saveLB(@PathVariable(value = "alsId") Long alsId,
                         @PathVariable(value = "lbId") Long lbId,
                         @ModelAttribute("lb") LB lb) {
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        Optional<LB> lbOptional = lbService.findById(lbId);
        LB lbOld = null;
        if (lbOptional.isPresent()) {lbOld = lbOptional.get();}
        als=alsService.updateLB(als,lb,lbOld);
        for(LB lb1:als.getLbList()){
            if(lb1.equals(lb)){
                lbId= (long) lb1.getId();
                break;
            }
        }
        return "redirect:/alss/" + als.getId()+"/lbs/"+lbId;
    }
}
