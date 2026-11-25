package com.lb_calc_web.controller;

import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.LCCatalogFilter;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.List;

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
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer heightMin,
            @RequestParam(required = false) Integer heightMax,
            @RequestParam(required = false) Integer widthMin,
            @RequestParam(required = false) Integer widthMax,
            @RequestParam(required = false) Integer depthMin,
            @RequestParam(required = false) Integer depthMax,
            @RequestParam(required = false) DisplayPurpose displayPurpose,
            @RequestParam(required = false) AccessPurpose accessPurpose,
            @RequestParam(required = false) PaymentPurpose paymentPurpose,
            @RequestParam(required = false) PrinterPurpose printerPurpose,
            @RequestParam(required = false) Colors colorBody,
            @RequestParam(required = false) Colors colorDoor,
            Model model
    ) {
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id",
                Set.of("id", "name", "height", "width", "depth", "displayPurpose",
                        "accessSort", "paymentPurpose", "printerPurpose", "colorBody", "colorDoor"));
        LCCatalogFilter filter = new LCCatalogFilter(
                id, name, heightMin, heightMax, widthMin, widthMax, depthMin, depthMax,
                displayPurpose, accessPurpose, paymentPurpose, printerPurpose, colorBody, colorDoor);
        var result = lcService.findPage(filter, pageable).map(
                com.lb_calc_web.mapper.dto.LCDtoMapper::toDto);
        model.addAttribute("lcs", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("filters", filter);
        model.addAttribute("sort", Set.of("id", "name", "height", "width", "depth",
                "displayPurpose", "accessSort", "paymentPurpose", "printerPurpose",
                "colorBody", "colorDoor").contains(sort) ? sort : "id");
        model.addAttribute("direction", pageable.getSort().stream().findFirst()
                .map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
        model.addAttribute("colorsList", List.of(Colors.values()));
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