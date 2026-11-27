package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.LCCatalogFilter;
import com.lb_calc_web.service.ModuleCatalogFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/** MVC-контроллер главной страницы и каталогов модулей и автоматизированных камер хранения. */
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

    /** Отображает главную страницу приложения. */
    @GetMapping
    public String init() {
        return "index";
    }

    /** Отображает каталог конфигураций ALS с фильтрацией и сортировкой. */
    @GetMapping("/alss")
    public String alss(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @ModelAttribute ModuleCatalogFilter filter,
            Model model
    ) {
        Set<String> allowedSorts = Set.of("id", "name", "countCells", "height", "width", "depth",
                "upperFrame", "bottomFrame", "positionControlModule", "displayPurpose",
                "accessSort", "paymentPurpose", "printerPurpose", "colorBody", "colorDoor");
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id", allowedSorts);
        var result = alsService.findPage(filter, pageable).map(ALSDtoMapper::toDto);
        model.addAttribute("alss", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("filters", filter);
        model.addAttribute("sort", allowedSorts.contains(sort) ? sort : "id");
        model.addAttribute("direction", pageable.getSort().stream().findFirst()
                .map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("colorsList", List.of(Colors.values()));
        model.addAttribute("positionControlModuleList", List.of(PositionControlModule.values()));
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
        return "alss/alss";
    }

    /** Отображает каталог управляющих модулей LC с фильтрацией и сортировкой. */
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
                LCDtoMapper::toDto);
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

    /** Отображает каталог модулей хранения LB с фильтрацией и сортировкой. */
    @GetMapping("/lbs")
    public String lbs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @ModelAttribute ModuleCatalogFilter filter,
            Model model
    ) {
        Set<String> allowedSorts = Set.of("id", "name", "type", "countCells", "height", "width",
                "depth", "upperFrame", "bottomFrame", "directionDoorOpening", "colorBody", "colorDoor");
        Pageable pageable = CatalogPageSupport.pageable(page, size, sort, direction, "id", allowedSorts);
        var result = lbService.findPage(filter, pageable).map(
                LBDtoMapper::toDto);
        model.addAttribute("lbs", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("filters", filter);
        model.addAttribute("sort", allowedSorts.contains(sort) ? sort : "id");
        model.addAttribute("direction", pageable.getSort().stream().findFirst()
                .map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("directionDoorOpeningList", List.of(DirectionDoorOpening.values()));
        return "lbs/lbs";
    }
}