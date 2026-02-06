package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.utils.*;
import com.lb_calc_web.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/alss")
public class ALSController {
    private static final Logger logger = LoggerFactory.getLogger(ALSController.class);
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
    @GetMapping("/create")
    private String createALS(Model model) {
        ALSDTO als = alsService.createALS();
        model.addAttribute("als", als);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("positionLCList", positionLCList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("typeList", typeLbList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "/alss/als";
    }
    @PostMapping("/save")
    public String saveALS(@ModelAttribute("als") ALSDTO als,Model model) {
        logger.debug("Saving ALS...");
        List<String> errorALSList=SizeValidator.getErrorValidateALSSizesList(als);
        als = alsService.resizeLC(als);
        List<String> errorLCList= SizeValidator.getErrorValidateLCSizesList(als.getLC());
        als = alsService.resizeLBs(als);
        List<List<String>> errorLBLists=SizeValidator.getErrorValidateLBSizesLists(als);
        if (errorALSList.isEmpty() && errorLCList.isEmpty() && errorLBLists.isEmpty()) {
                als = alsService.saveALS(als);
        }
        else {
            logger.warn(String.valueOf(errorALSList));
            logger.warn(String.valueOf(errorLCList));
            logger.warn(String.valueOf(errorLBLists));
            model.addAttribute("ALSErrors", errorALSList);
            model.addAttribute("LCErrors", errorLCList);
            model.addAttribute("LBErrors", errorLBLists);
            model.addAttribute("als", als);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("positionLCList", positionLCList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("typeList", typeLbList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
            return "/alss/als";
        }
        return "redirect:/alss/" + als.getId();
    }
    @GetMapping("/{id}")
    private String editALS(@PathVariable(value = "id") Long id, Model model) {
        Optional<ALSDTO> alsOptional = alsService.findById(id);
        if (alsOptional.isPresent()) {
            ALSDTO als = alsOptional.get();
            model.addAttribute("als", als);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("positionLCList", positionLCList);
            model.addAttribute("typeList", typeLbList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
        }
        return "alss/als";
    }
    @PostMapping("/{alsId}/lbs/{lbId}/save")
    public String saveLBatALS(@PathVariable(value = "alsId") Long alsId,
                              @PathVariable(value = "lbId") Long lbId,
                              @ModelAttribute("lb") LBDTO lb,
                              Model model){
        logger.debug("Saving LB at ALS...");
        List<String> errorList= SizeValidator.getErrorValidateLBSizesList(lb);
        List<Object> ALSlbIdList;
        if (errorList.isEmpty()) {
            ALSlbIdList = alsService.replaceLBandSaveALS(alsId, lbId, lb);
        } else {
            logger.warn(errorList.toString());
            Optional<ALSDTO> alsOptional = alsService.findById(alsId);
            ALSDTO als = null;
            if (alsOptional.isPresent()) {als = alsOptional.get();}
            model.addAttribute("als", als);
            model.addAttribute("errors",errorList);
            model.addAttribute("lb", lb);
            model.addAttribute("typeLbList", typeLbList);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
            return "alss/alss_lb";
        }

         ALSDTO als= (ALSDTO) ALSlbIdList.get(0);
         int newLbId= (int) ALSlbIdList.get(1);
         model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId()+"/lbs/"+newLbId;
    }
    @GetMapping("/{alsId}/addLB")
    public String addLBatALS(@PathVariable(value = "alsId") Long alsId,
                        Model model) {
        logger.debug("Adding LB at ALS...");

        ALSDTO als = alsService.addNewLBandSaveALS(alsId);
        model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId();
    }

    @GetMapping("/{alsId}/lbs/{lbId}")
    private String editLBatALS(@PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lbId") Long lbId,
                          Model model) {

        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("als", als);
        Optional<LBDTO> lbOptional = lbService.findById(lbId);
        LBDTO lb = null;
        if (lbOptional.isPresent()) {lb = lbOptional.get();}
        model.addAttribute("lb", lb);

        model.addAttribute("typeLbList", typeLbList);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("directionDoorOpeningList", directionDoorOpeningList);
        return "alss/alss_lb";
    }

    @GetMapping("/{alsId}/lbs/{lbId}/delete")
    public String deleteLBatALS(@PathVariable(value = "alsId") Long alsId,
                           @PathVariable(value = "lbId") Long lbId,
                           Model model)  {
        ALSDTO als=alsService.deleteLBandSaveALS(alsId, lbId);
        model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId();
    }

    @GetMapping("/{alsId}/lcs/{lcId}")
    private String editLCatALS(@PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lcId") Long lcId,
                          Model model) {

        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        model.addAttribute("als", als);
        Optional<LCDTO> lcOptional = lcService.findById(lcId);
        LCDTO lc = null;
        if (lcOptional.isPresent()) {lc = lcOptional.get();}
        model.addAttribute("lc", lc);
        model.addAttribute("colorsList", colorsList);
        model.addAttribute("paymentList", paymentList);
        model.addAttribute("displayList", displayList);
        model.addAttribute("barReaderList", barReaderList);
        return "alss/alss_lc";
    }
    @PostMapping("/{alsId}/lcs/{lcId}/save")
    public String saveLCatALS(
            @PathVariable(value = "alsId") Long alsId,
            @PathVariable(value = "lcId") Long lcId,
            @ModelAttribute("lc") LCDTO lc,
            Model model)  {
        logger.debug("Saving LC at ALS...");
        List<String> errorList =SizeValidator.getErrorValidateLCSizesList(lc);
        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        if (errorList.isEmpty()) {
            als=alsService.replaceLCandSaveALS(als,lc);
        }
        else {
            logger.warn(errorList.toString());
            model.addAttribute("lc", lc);
            model.addAttribute("errors", errorList);
            model.addAttribute("colorsList", colorsList);
            model.addAttribute("paymentList", paymentList);
            model.addAttribute("displayList", displayList);
            model.addAttribute("barReaderList", barReaderList);
            return "alss/alss_lc";
        }
        return "redirect:/alss/" + als.getId()+"/lcs/"+als.getLC().getId();
    }
}
