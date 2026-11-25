package com.lb_calc_web.controller;

import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

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
        return "index";
    }

    @GetMapping("/alss")
    public String alss(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "height", "width", "depth"));
        var result = alsService.findPage(q, pageable).map(ALSDtoMapper::toDto);
        model.addAttribute("alss", result.getContent());
        CatalogPageSupport.addModelAttributes(model, result, q, pageable, "/alss",
                CatalogPageSupport.sortOptions("id", "ID", "name", "Название",
                        "height", "Высота", "width", "Ширина", "depth", "Глубина"));
        return "alss/alss";
    }

    @GetMapping("/lcs")
    public String lcs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "height", "width", "depth"));
        var result = lcService.findPage(q, pageable).map(
                com.lb_calc_web.mapper.dto.LCDtoMapper::toDto);
        model.addAttribute("lcs", result.getContent());
        CatalogPageSupport.addModelAttributes(model, result, q, pageable, "/lcs",
                CatalogPageSupport.sortOptions("id", "ID", "name", "Название",
                        "height", "Высота", "width", "Ширина", "depth", "Глубина"));
        return "lcs/lcs";
    }

    @GetMapping("/lbs")
    public String lbs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "type", "countCells", "height", "width", "depth"));
        var result = lbService.findPage(q, pageable).map(
                com.lb_calc_web.mapper.dto.LBDtoMapper::toDto);
        model.addAttribute("lbs", result.getContent());
        CatalogPageSupport.addModelAttributes(model, result, q, pageable, "/lbs",
                CatalogPageSupport.sortOptions("id", "ID", "name", "Название", "type", "Тип",
                        "countCells", "Ячейки", "height", "Высота", "width", "Ширина",
                        "depth", "Глубина"));
        return "lbs/lbs";
    }
}