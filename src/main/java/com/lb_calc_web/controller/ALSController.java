package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.attributes.*;
import com.lb_calc_web.service.*;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/alss")
public class ALSController extends BaseCatalogController {
    private static final Logger logger = LoggerFactory.getLogger(ALSController.class);
    private final ALSService alsService;
    private final LCService lcService;
    private final LBService lbService;
    @Autowired
    public ALSController(ALSService alsService, LCService lcService, LBService lbService) {
        this.alsService = alsService;
        this.lcService = lcService;
        this.lbService = lbService;
    }
    @GetMapping("/create")
    private String createALS(Model model) {
        ALSDTO als = alsService.createALS();
        model.addAttribute("als", als);
        return "/alss/als";
    }
    @PostMapping("/save")
    public String saveALS(@ModelAttribute("als") ALSDTO als,Model model) {
        List<String> errorALSList= SizeValidator.getErrorValidateALSSizesList(als);
        als = alsService.resizeLC(als);
        List<String> errorLCList= SizeValidator.getErrorValidateLCSizesList(als.getLC());
        als = alsService.resizeLBs(als);
        List<List<String>> errorLBLists=SizeValidator.getErrorValidateLBSizesLists(als);
        if (errorALSList.isEmpty() && errorLCList.isEmpty() && errorLBLists.isEmpty()) {
                als = alsService.saveALS(als);
        }
        else {
            logger.warn("[Ошибки размеров АКХ]:"+ errorALSList);
            logger.warn("[Ошибки размеров МХ]:"+ errorLCList);
            logger.warn("[Ошибки размеров МУ]:"+errorLBLists);
            model.addAttribute("ALSErrors", errorALSList);
            model.addAttribute("LCErrors", errorLCList);
            model.addAttribute("LBErrors", errorLBLists);
            model.addAttribute("als", als);
            return "/alss/als";
        }
        return "redirect:/alss/" + als.getId();
    }
    @GetMapping("/{id}")
    private String editALS(@PathVariable(value = "id") Long id, Model model) {
            ALSDTO als = alsService.findById(id);
            model.addAttribute("als", als);

        return "alss/als";
    }
    @PostMapping("/{alsId}/lbs/{lbId}/save")
    public String saveLBatALS(@PathVariable(value = "alsId") Long alsId,
                              @PathVariable(value = "lbId") Long lbId,
                              @ModelAttribute("lb") LBDTO lb,
                              Model model){
        List<String> errorList= SizeValidator.getErrorValidateLBSizesList(lb);
        List<Object> ALSlbIdList;
        if (errorList.isEmpty()) {
            ALSlbIdList = alsService.replaceLBandSaveALS(alsId, lbId, lb);
        } else {
            logger.warn(errorList.toString());
            ALSDTO als = alsService.findById(alsId);
            model.addAttribute("als", als);
            model.addAttribute("errors",errorList);
            model.addAttribute("lb", lb);
            return "alss/alss_lb";
        }

         ALSDTO als= (ALSDTO) ALSlbIdList.get(0);
         int newLbId= (int) ALSlbIdList.get(1);
         model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId()+"/lbs/"+newLbId;
    }
    @PostMapping("/{alsId}/addLB")
    public String addLBatALS(@PathVariable(value = "alsId") Long alsId,
                        Model model) {
        ALSDTO als = alsService.addNewLBandSaveALS(alsId);
        model.addAttribute("als", als);
        return "redirect:/alss/" + als.getId();
    }

    @GetMapping("/{alsId}/lbs/{lbId}")
    private String editLBatALS(@PathVariable(value = "alsId") Long alsId,
                          @PathVariable(value = "lbId") Long lbId,
                          Model model) {
        ALSDTO als = alsService.findById(alsId);
              model.addAttribute("als", als);
        LBDTO lb = lbService.findById(lbId);
        model.addAttribute("lb", lb);
        return "alss/alss_lb";
    }

    @PostMapping("/{alsId}/lbs/{lbId}/delete")
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
        ALSDTO als = alsService.findById(alsId);
        model.addAttribute("als", als);
        LCDTO lc = lcService.findById(lcId);
        model.addAttribute("lc", lc);
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
        ALSDTO als = alsService.findById(alsId);
        if (errorList.isEmpty()) {
            als=alsService.replaceLCandSaveALS(als,lc);
        }
        else {
            logger.warn(errorList.toString());
            model.addAttribute("lc", lc);
            model.addAttribute("errors", errorList);
            return "alss/alss_lc";
        }
        return "redirect:/alss/" + als.getId()+"/lcs/"+als.getLC().getId();
    }
}
