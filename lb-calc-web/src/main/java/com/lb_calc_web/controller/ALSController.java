package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.service.image.ALSImageService;
import com.lb_calc_web.service.image.LBCImageService;
import com.lb_calc_web.service.image.LCImageService;
import com.lb_calc_web.service.image.LBImageService;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.ALSService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Controller
@RequestMapping("/alss")
public class ALSController extends BaseCatalogController {

    private final ALSService alsService;
    private final EquipmentCatalogService equipmentCatalogService;

    public ALSController(
            ALSService alsService,
            EquipmentCatalogService equipmentCatalogService
    ) {
        this.alsService = alsService;
        this.equipmentCatalogService = equipmentCatalogService;
    }

    @GetMapping("/create")
    public String createALS(Model model) {
        model.addAttribute("pageContext", "CATALOG");
        ALSDTO als = ALSImageService.addImages(ALSDtoMapper.toDto(alsService.createALS()));
        model.addAttribute("als", als);
        return "alss/als";
    }

    @GetMapping("/{id}")
    public String editALS(@PathVariable Long id, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        ALSDTO als = ALSImageService.addImages(ALSDtoMapper.toDto(alsService.findById(id)));
        model.addAttribute("als", als);
        return "alss/als";
    }

    @PostMapping("/save")
    public String saveALS(@ModelAttribute("als") ALSDTO als, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            // Главная форма ALS редактирует параметры агрегата, но не состав LC/LBC.
            // Восстанавливаем управляющий модуль из БД, чтобы форма не теряла
            // каталожное оборудование, которое не редактируется на этой странице.
            if (als.getId() != null && als.getId() > 0) {
                ALSDTO persisted = ALSDtoMapper.toDto(alsService.findById(als.getId()));
                als.setLC(persisted.getLC());
                als.setLBC(persisted.getLBC());
            }

            com.lb_calc_web.domain.model.ALS domain = alsService.restoreDomain(als);
            com.lb_calc_web.domain.model.ALS saved = alsService.saveALS(domain);
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("ALSErrors", ValidationErrorFormatter.format(e.getValidationResult()));
            model.addAttribute("als", ALSImageService.addImages(als));
            return "alss/als";
        }
    }

    @PostMapping("/{alsId}/addLB")
    public String addLB(@PathVariable Long alsId) {
        ALSDTO als = ALSDtoMapper.toDto(alsService.addNewLBandSaveALS(alsId));
        return "redirect:/alss/" + als.getId();
    }

    /**
     * Форма редактирования LB внутри ALS.
     */
    @GetMapping("/{alsId}/lbs/{lbId}")
    public String editLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId,
            Model model
    ) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        ALS als = alsService.findById(alsId);
        LB lb = findLBInALS(als, lbId);

        model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(als)));
        model.addAttribute("lb", LBImageService.addImage(LBDtoMapper.toDto(lb)));
        return "lbs/lb";
    }

    /**
     * Сохраняет изменения выбранного LB внутри ALS.
     */
    @PostMapping("/{alsId}/lbs/{lbId}/save")
    public String saveLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId,
            @ModelAttribute("lb") LBDTO lb,
            Model model
    ) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        try {
            ALS als = alsService.findById(alsId);
            findLBInALS(als, lbId);
            lb.setId(lbId);

            LB replacement = alsService.restoreLB(lb);
            ALS saved = alsService.saveALS(alsService.replaceLB(als, lbId, replacement));
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            ALS als = alsService.findById(alsId);
            model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(als)));
            model.addAttribute("lb", LBImageService.addImage(lb));
            return "lbs/lb";
        }
    }

    private LB findLBInALS(ALS als, Long lbId) {
        return als.getModules().stream()
                .filter(LB.class::isInstance)
                .map(LB.class::cast)
                .filter(candidate -> Objects.equals(candidate.getId(), lbId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "LB с id " + lbId + " не найден в ALS " + als.getId()));
    }

    /**
     * Форма редактирования LC внутри ALS.
     */
    @GetMapping("/{alsId}/lcs/{lcId}")
    public String editLC(
            @PathVariable Long alsId,
            @PathVariable Long lcId,
            Model model
    ) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        com.lb_calc_web.domain.model.ALS domain = alsService.findById(alsId);
        com.lb_calc_web.domain.model.LC lc = domain.getLC();
        if (lc == null || !java.util.Objects.equals(lc.getId(), lcId)) {
            throw new java.util.NoSuchElementException(
                    "LC с id " + lcId + " не найден в ALS " + alsId);
        }

        model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(domain)));
        prepareLCForm(model, LCImageService.addImage(LCDtoMapper.toDto(lc)));
        return "lcs/lc";
    }

    /**
     * Сохраняет изменения LC в составе ALS.
     */
    @PostMapping("/{alsId}/lcs/{lcId}/save")
    public String saveLC(
            @PathVariable Long alsId,
            @PathVariable Long lcId,
            @ModelAttribute("lc") LCDTO lc,
            Model model
    ) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        try {
            com.lb_calc_web.domain.model.ALS als = alsService.findById(alsId);
            if (als.getLC() == null || !java.util.Objects.equals(als.getLC().getId(), lcId)) {
                throw new java.util.NoSuchElementException(
                        "LC с id " + lcId + " не найден в ALS " + alsId);
            }
            lc.setId(lcId);

            com.lb_calc_web.domain.model.LC replacement = alsService.restoreLC(lc, als);
            com.lb_calc_web.domain.model.ALS saved =
                    alsService.saveALS(alsService.replaceLC(als, replacement));
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            com.lb_calc_web.domain.model.ALS als = alsService.findById(alsId);
            model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(als)));
            prepareLCForm(model, LCImageService.addImage(lc));
            return "lcs/lc";
        }
    }

    /** Обновляет комплектацию LC внутри ALS без сохранения ALS. */
    @PostMapping("/{alsId}/lcs/{lcId}/refresh")
    public String refreshLC(@PathVariable Long alsId, @PathVariable Long lcId,
                            @ModelAttribute("lc") LCDTO lc, Model model) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        com.lb_calc_web.domain.model.ALS source = alsService.findById(alsId);
        if (source.getLC() == null || !Objects.equals(source.getLC().getId(), lcId))
            throw new NoSuchElementException("LC с id " + lcId + " не найден в ALS " + alsId);
        lc.setId(lcId);
        model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(source)));
        prepareLCForm(model, LCImageService.addImage(lc));
        return "lcs/lc";
    }

    private void prepareLCForm(Model model, LCDTO lc) {
        model.addAttribute("lc", lc);
        lc.setEquipmentSelections(equipmentCatalogService.prepareSelections(
                lc.getFunctionalConfiguration(), lc.getEquipmentSelections()));
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }

    /**
     * Форма редактирования LBC внутри ALS.
     */
    @GetMapping("/{alsId}/lbcs/{lbcId}")
    public String editLBC(
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            Model model
    ) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        com.lb_calc_web.domain.model.ALS domain = alsService.findById(alsId);
        ALSDTO als = ALSImageService.addImages(ALSDtoMapper.toDto(domain));

        model.addAttribute("als", als);
        prepareLBCForm(model,
                LBCImageService.addImage(LBCDtoMapper.toDto(alsService.findLBCInALS(domain, lbcId))));
        return "lbcs/lbc";
    }

    /**
     * Сохранение LBC внутри ALS.
     */
    @PostMapping("/{alsId}/lbcs/{lbcId}/save")
    public String saveLBC(
            @PathVariable Long alsId,
            @PathVariable Long lbcId,
            @ModelAttribute("lbc") LBCDTO lbc,
            Model model
    ) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        try {
            com.lb_calc_web.domain.model.ALS als = alsService.findById(alsId);
            lbc.setId(lbcId);

            com.lb_calc_web.domain.model.LBC replacement = alsService.restoreLBC(lbc, als);
            com.lb_calc_web.domain.model.ALS saved =
                    alsService.saveALS(alsService.replaceLBC(als, replacement));
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(alsService.findById(alsId))));
            prepareLBCForm(model, LBCImageService.addImage(lbc));
            return "lbcs/lbc";
        }
    }

    /** Обновляет комплектацию LBC внутри ALS без сохранения ALS. */
    @PostMapping("/{alsId}/lbcs/{lbcId}/refresh")
    public String refreshLBC(@PathVariable Long alsId, @PathVariable Long lbcId,
                             @ModelAttribute("lbc") LBCDTO lbc, Model model) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        com.lb_calc_web.domain.model.ALS source = alsService.findById(alsId);
        alsService.findLBCInALS(source, lbcId);
        lbc.setId(lbcId);
        model.addAttribute("als", ALSImageService.addImages(ALSDtoMapper.toDto(source)));
        prepareLBCForm(model, LBCImageService.addImage(lbc));
        return "lbcs/lbc";
    }

    private void prepareLBCForm(Model model, LBCDTO lbc) {
        model.addAttribute("lbc", lbc);
        lbc.setEquipmentSelections(equipmentCatalogService.prepareSelections(
                lbc.getFunctionalConfiguration(), lbc.getEquipmentSelections()));
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        model.addAttribute("displayPurposes", List.of(DisplayPurpose.values()));
        model.addAttribute("accessPurposes", List.of(AccessPurpose.values()));
        model.addAttribute("paymentPurposes", List.of(PaymentPurpose.values()));
        model.addAttribute("printerPurposes", List.of(PrinterPurpose.values()));
    }

    /**
     * Заменяет текущий control-модуль на новый LBC.
     */
    @PostMapping("/{alsId}/replaceLBC")
    public String replaceLBC(@PathVariable Long alsId) {
        com.lb_calc_web.domain.model.ALS saved = alsService.replaceWithNewLBCandSaveALS(alsId);
        return "redirect:/alss/" + saved.getId();
    }

    @PostMapping("/{alsId}/lbs/{lbId}/delete")
    public String deleteLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {
        ALSDTO als = ALSDtoMapper.toDto(alsService.deleteLBandSaveALS(alsId, lbId));
        return "redirect:/alss/" + als.getId();
    }
}
