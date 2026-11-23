package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.EquipmentSelectionMapper;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.service.image.LBCImageService;
import com.lb_calc_web.domain.equipment.Equipment;
import org.springframework.core.env.Environment;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.LBCService;
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
@RequestMapping("/lbcs")
public class LBCController extends BaseCatalogController {

    private static final Logger logger =
            LoggerFactory.getLogger(LBCController.class);

    private final LBCService lbcService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final Environment environment;

    public LBCController(LBCService lbcService, EquipmentCatalogService equipmentCatalogService,
                         Environment environment) {
        this.lbcService = lbcService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.environment = environment;
    }

    /**
     * Список всех LBC.
     */
    @GetMapping
    public String lbcs(Model model) {
        model.addAttribute(
                "lbcs",
                lbcService.findAll().stream().map(LBCDtoMapper::toDto).map(LBCController::addLBCImage).toList()
        );

        return "lbcs/lbcs";
    }

    /**
     * Создание нового LBC.
     */
    @GetMapping("/create")
    public String createLBC(Model model) {
        logger.info("Открытие страницы создания LBC");

        model.addAttribute(
                "lbc",
                addLBCImage(LBCDtoMapper.toDto(lbcService.createLBC()))
        );

        return "lbcs/lbc";
    }

    /**
     * Редактирование существующего LBC.
     */
    @GetMapping("/{id}")
    public String editLBC(
            @PathVariable Long id,
            Model model
    ) {
        logger.info(
                "Открытие страницы редактирования LBC id={}",
                id
        );

        model.addAttribute(
                "lbc",
                addLBCImage(LBCDtoMapper.toDto(lbcService.findById(id)))
        );

        return "lbcs/lbc";
    }

    /**
     * Сохранение LBC.
     */
    @PostMapping("/save")
    public String saveLBC(
            @ModelAttribute("lbc") LBCDTO lbc,
            Model model
    ) {
        try {
            LBC saved = lbcService.saveLBC(toDomain(lbc));

            return "redirect:/lbcs/" + saved.getId();

        } catch (ValidationSizeException e) {
            model.addAttribute(
                    "errors",
                    e.getErrors()
            );
            model.addAttribute(
                    "lbc",
                    lbc
            );

            return "lbcs/lbc";
        }
    }

    private LBC toDomain(LBCDTO dto) {
        try {
            return mapToDomain(dto);
        } catch (ValidationSizeException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            ValidationResult result = new ValidationResult("LBC", dto.getId());
            result.addError("domainModel", exception.getMessage(), null, null, null);
            throw new ValidationSizeException(result);
        }
    }

    private LBC mapToDomain(LBCDTO dto) {
        LBC existing = dto.getId() == null || dto.getId() <= 0
                ? null
                : lbcService.findById(dto.getId());
        Set<String> retainedIds = existing == null ? Set.of()
                : existing.getEquipment().stream()
                        .map(item -> item.getCatalogPositionId())
                        .collect(Collectors.toUnmodifiableSet());
        Function<String, Equipment> resolver = existing == null
                ? equipmentCatalogService::resolve
                : equipmentCatalogService.findExistingConfigurationResolver(retainedIds);

        List<EquipmentSelectionDTO> selections = dto.getEquipmentSelections();
        if (selections == null && existing != null) {
            selections = EquipmentSelectionMapper.mergeDisplaySelection(
                    LBCDtoMapper.toDto(existing).getEquipmentSelections(),
                    dto.getDisplay(),
                    dto.getDisplayQuantity(),
                    resolver
            );
        }
        return LBCDtoMapper.toDomain(
                dto,
                TypeLbFactory.forLBC(environment, dto.getType()),
                selections,
                resolver
        );
    }

    private static LBCDTO addLBCImage(LBCDTO dto) {
        dto.setStringLBCImage(LBCImageService.getStringLBCImage(dto));
        return dto;
    }

    @ModelAttribute("accessMethodList")
    public List<AccessMethod> accessMethodList() {
        return Arrays.asList(AccessMethod.values());
    }

    @ModelAttribute("printOptionList")
    public List<PrintOption> printOptionList() {
        return Arrays.asList(PrintOption.values());
    }
}
