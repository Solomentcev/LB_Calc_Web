package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.EquipmentSelectionMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.LCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lcs")
public class LCController {

    private static final Logger logger =
            LoggerFactory.getLogger(LCController.class);

    private final LCService lcService;
    private final EquipmentCatalogService equipmentCatalogService;

    private final List<Colors> colorsList =
            Arrays.asList(Colors.values());

    private final List<Payment> paymentList =
            Arrays.asList(Payment.values());

    private final List<AccessMethod> accessMethodList =
            Arrays.asList(AccessMethod.values());

    private final List<String> displayList =
            List.of(
                    Display.NONE.getName(),
                    Display.LC10.getName(),
                    Display.LC17.getName(),
                    Display.LC19.getName()
            );

    private final List<String> barReaderList =
            List.of(
                    BarReader.NONE.getName(),
                    BarReader.READER_1D.getName(),
                    BarReader.READER_2D.getName()
            );

    public LCController(LCService lcService, EquipmentCatalogService equipmentCatalogService) {
        this.lcService = lcService;
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /**
     * Создание нового LC.
     */
    @GetMapping("/create")
    public String createLC(Model model) {

        logger.info("Открытие страницы создания LC");

        LCDTO lc = LCDtoMapper.toDto(lcService.createLC());
        lc.setId(0L);

        model.addAttribute("lc", lc);

        return "lcs/lc";
    }

    /**
     * Редактирование существующего LC.
     */
    @GetMapping("/{id}")
    public String editLC(
            @PathVariable Long id,
            Model model
    ) {

        logger.info(
                "Открытие страницы редактирования LC id={}",
                id
        );

        LCDTO lc = LCDtoMapper.toDto(lcService.findById(id));

        model.addAttribute("lc", lc);

        return "lcs/lc";
    }

    /**
     * Сохранение LC.
     */
    @PostMapping("/save")
    public String saveLC(
            @ModelAttribute("lc") LCDTO lc,
            Model model
    ) {

        logger.info(
                "Сохранение LC id={}, display={}",
                lc.getId(),
                lc.getDisplay()
        );

        try {

            LC savedLC = lcService.saveLC(toDomain(lc));

            return "redirect:/lcs/" + savedLC.getId();

        } catch (ValidationSizeException e) {

            logger.warn(
                    "LC не прошёл валидацию: {}",
                    e.getErrors()
            );

            model.addAttribute(
                    "errors",
                    e.getErrors()
            );

            model.addAttribute(
                    "lc",
                    lc
            );

            return "lcs/lc";
        }
    }

    private LC toDomain(LCDTO dto) {
        LC existing = dto.getId() == null || dto.getId() <= 0
                ? null
                : lcService.findById(dto.getId());
        Set<String> retainedIds = existing == null ? Set.of()
                : existing.getEquipment().stream()
                        .map(item -> item.getCatalogPositionId())
                        .collect(Collectors.toUnmodifiableSet());
        Function<String, com.lb_calc_web.domain.equipment.Equipment> resolver =
                existing == null
                        ? equipmentCatalogService::resolve
                        : equipmentCatalogService.findExistingConfigurationResolver(retainedIds);

        List<EquipmentSelectionDTO> selections = dto.getEquipmentSelections();
        if (selections == null && existing != null) {
            selections = EquipmentSelectionMapper.mergeDisplaySelection(
                    LCDtoMapper.toDto(existing).getEquipmentSelections(),
                    dto.getDisplay(),
                    dto.getDisplayQuantity(),
                    resolver
            );
        }
        return LCDtoMapper.toDomain(dto, selections, resolver);
    }

    /**
     * Список цветов.
     */
    @ModelAttribute("colorsList")
    public List<Colors> colorsList() {
        return colorsList;
    }

    /**
     * Список способов оплаты.
     */
    @ModelAttribute("paymentList")
    public List<Payment> paymentList() {
        return paymentList;
    }

    /**
     * Список способов доступа.
     */
    @ModelAttribute("accessMethodList")
    public List<AccessMethod> accessMethodList() {
        return accessMethodList;
    }

    /**
     * Список доступных дисплеев.
     */
    @ModelAttribute("displayList")
    public List<String> displayList() {
        return displayList;
    }

    /**
     * Список доступных сканеров штрихкода.
     */
    @ModelAttribute("barReaderList")
    public List<String> barReaderList() {
        return barReaderList;
    }
}