package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.ControlModule;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LBC;
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
import com.lb_calc_web.domain.geometry.SizeBounds;
import com.lb_calc_web.service.factory.TypeLbFactory;
import org.springframework.core.env.Environment;
import com.lb_calc_web.service.ALSService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/** MVC-контроллер создания и редактирования конфигураций ALS и вложенных модулей. */
@Controller
@RequestMapping("/alss")
public class ALSController extends BaseCatalogController {

    private final ALSService alsService;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;
    private final Environment environment;

    public ALSController(
            ALSService alsService,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds,
            Environment environment
    ) {
        this.alsService = alsService;
        this.equipmentCatalogService = equipmentCatalogService;
        this.sizeBounds = sizeBounds;
        this.environment = environment;
    }

    /** Открывает форму создания новой конфигурации ALS. */
    @GetMapping("/create")
    public String createALS(Model model) {
        model.addAttribute("pageContext", "CATALOG");
        ALSDTO als = toDtoWithImages(alsService.createALS());
        model.addAttribute("als", als);
        return "alss/als";
    }

    /** Открывает форму редактирования сохранённой конфигурации ALS. */
    @GetMapping("/{id}")
    public String editALS(@PathVariable Long id, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        ALSDTO als = toDtoWithImages(alsService.findById(id));
        model.addAttribute("als", als);
        return "alss/als";
    }

    /**
     * Пересчитывает ALS и обновляет форму без сохранения конфигурации.
     */
    @PostMapping("/refresh")
    public String refreshALS(@ModelAttribute("als") ALSDTO als, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            ALS existing = als.getId() != null && als.getId() > 0
                    ? alsService.findById(als.getId()) : null;
            if (existing != null) {
                ALSDTO persisted = ALSDtoMapper.toDto(existing);
                als.setLC(persisted.getLC());
                als.setLBC(persisted.getLBC());
            }
            ALS restored = ALSDtoMapper.toDomain(
                    als,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            restored = alsService.prepareConfiguration(existing, restored, als.isEquipmentSelectionProvided());
            alsService.validateForSave(restored);
            als = toDtoWithImages(restored);
        } catch (ValidationSizeException exception) {
            model.addAttribute("ALSErrors",
                    ValidationErrorFormatter.format(exception.getValidationResult()));
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException exception) {
            model.addAttribute("ALSErrors", List.of(exception.getMessage()));
        }
        model.addAttribute("als", als);
        return "alss/als";
    }

    /** Проверяет и сохраняет конфигурацию ALS. */
    @PostMapping("/save")
    public String saveALS(@ModelAttribute("als") ALSDTO als, Model model) {
        model.addAttribute("pageContext", "CATALOG");
        try {
            // Главная форма ALS редактирует параметры агрегата, но не состав LC/LBC.
            // Восстанавливаем управляющий модуль из БД, чтобы форма не теряла
            // каталожное оборудование, которое не редактируется на этой странице.
            ALS existing = als.getId() != null && als.getId() > 0
                    ? alsService.findById(als.getId()) : null;
            if (existing != null) {
                ALSDTO persisted = ALSDtoMapper.toDto(existing);
                als.setLC(persisted.getLC());
                als.setLBC(persisted.getLBC());
            }

            ALS restored = ALSDtoMapper.toDomain(
                    als,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    equipmentCatalogService::findById,
                    sizeBounds
            );
            ALS domain = alsService.prepareConfiguration(existing, restored, als.isEquipmentSelectionProvided());
            ALS saved = alsService.saveALS(domain);
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("ALSErrors", ValidationErrorFormatter.format(e.getValidationResult()));
            model.addAttribute("als", als);
            return "alss/als";
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            model.addAttribute("ALSErrors", List.of(e.getMessage()));
            model.addAttribute("als", als);
            return "alss/als";
        }
    }

    /** Добавляет новый модуль хранения LB в редактируемую ALS. */
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

        model.addAttribute("als", toDtoWithImages(als));
        model.addAttribute("lb", toDtoWithImage(lb));
        return "lbs/lb";
    }

    /**
     * Пересчитывает LB внутри ALS и обновляет форму без сохранения ALS.
     */
    @PostMapping("/{alsId}/lbs/{lbId}/refresh")
    public String refreshLB(@PathVariable Long alsId, @PathVariable Long lbId,
                            @ModelAttribute("lb") LBDTO lb, Model model) {
        model.addAttribute("pageContext", "ALS_CATALOG");
        ALS als = alsService.findById(alsId);
        findLBInALS(als, lbId);
        lb.setId(lbId);
        try {
            LB replacement = LBDtoMapper.toDomain(lb, TypeLbFactory.forLB(environment, lb.getType()));
            als = alsService.replaceLB(als.copy(), lbId, replacement);
            lb = toDtoWithImage(replacement);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errors", List.of("Проверьте введённые параметры модуля."));
        }
        model.addAttribute("als", toDtoWithImages(als));
        model.addAttribute("lb", lb);
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

            LB replacement = LBDtoMapper.toDomain(lb, TypeLbFactory.forLB(environment, lb.getType()));
            ALS saved = alsService.saveALS(alsService.replaceLB(als, lbId, replacement));
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            ALS als = alsService.findById(alsId);
            model.addAttribute("als", toDtoWithImages(als));
            model.addAttribute("lb", lb);
            return "lbs/lb";
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            model.addAttribute("errors", List.of(exception.getMessage()));
            ALS als = alsService.findById(alsId);
            model.addAttribute("als", toDtoWithImages(als));
            model.addAttribute("lb", lb);
            return "lbs/lb";
        }
    }

    private ALSDTO toDtoWithImages(ALS domain) {
        ALSDTO dto = ALSDtoMapper.toDto(domain);
        dto.setStringALSImage(ALSImageService.getStringALSImage(domain));
        int lbIndex = 0;
        for (com.lb_calc_web.domain.model.Module module : domain.getModules()) {
            if (module instanceof LB lb) {
                dto.getLbList().get(lbIndex++).setStringLBImage(LBImageService.getStringLBImage(lb));
            } else if (module instanceof com.lb_calc_web.domain.model.LC lc) {
                dto.getLC().setStringLCImage(LCImageService.getStringLCImage(lc));
            } else if (module instanceof com.lb_calc_web.domain.model.LBC lbc) {
                dto.getLBC().setStringLBCImage(LBCImageService.getStringLBCImage(lbc));
            }
        }
        return dto;
    }

    private LBDTO toDtoWithImage(LB domain) {
        LBDTO dto = LBDtoMapper.toDto(domain);
        dto.setStringLBImage(LBImageService.getStringLBImage(domain));
        return dto;
    }

    private LCDTO toDtoWithImage(com.lb_calc_web.domain.model.LC domain) {
        LCDTO dto = LCDtoMapper.toDto(domain);
        dto.setStringLCImage(LCImageService.getStringLCImage(domain));
        return dto;
    }

    private LBCDTO toDtoWithImage(com.lb_calc_web.domain.model.LBC domain) {
        LBCDTO dto = LBCDtoMapper.toDto(domain);
        dto.setStringLBCImage(LBCImageService.getStringLBCImage(domain));
        return dto;
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

        model.addAttribute("als", toDtoWithImages(domain));
        model.addAttribute("lc", toDtoWithImage(lc));
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
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

            LC replacement = LCDtoMapper.toDomain(
                    lc, equipmentCatalogService::findById, sizeBounds);
            com.lb_calc_web.domain.model.ALS saved =
                    alsService.replaceLCandSaveALS(als, replacement, lc.isEquipmentProvided());
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            com.lb_calc_web.domain.model.ALS als = alsService.findById(alsId);
            model.addAttribute("als", toDtoWithImages(als));
            model.addAttribute("lc", lc);
            model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
            return "lcs/lc";
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException exception) {
            model.addAttribute("errors", List.of(exception.getMessage()));
            ALS als = alsService.findById(alsId);
            model.addAttribute("als", toDtoWithImages(als));
            model.addAttribute("lc", lc);
            model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
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
        try {
            LC replacement = LCDtoMapper.toDomain(
                    lc, equipmentCatalogService::findById, sizeBounds);
            alsService.prepareForReplacement(source, replacement, lc.isEquipmentProvided());
            source = alsService.replaceLC(source.copy(), replacement);
            lc = toDtoWithImage(replacement);
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("errors", List.of(exception.getMessage()));
        }
        model.addAttribute("als", toDtoWithImages(source));
        model.addAttribute("lc", lc);
            model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        return "lcs/lc";
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
        ALSDTO als = toDtoWithImages(domain);

        model.addAttribute("als", als);
        model.addAttribute("lbc", toDtoWithImage(alsService.findLBCInALS(domain, lbcId)));
        model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
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

            LBC replacement = LBCDtoMapper.toDomain(
                    lbc, TypeLbFactory.forLBC(environment, lbc.getType()),
                    equipmentCatalogService::findById, sizeBounds);
            com.lb_calc_web.domain.model.ALS saved =
                    alsService.replaceLBCandSaveALS(als, replacement, lbc.isEquipmentProvided());
            return "redirect:/alss/" + saved.getId();
        } catch (ValidationSizeException e) {
            model.addAttribute("errors", ValidationErrorFormatter.format(e.getValidationResult()));
            model.addAttribute("als", toDtoWithImages(alsService.findById(alsId)));
            model.addAttribute("lbc", lbc);
            model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
            return "lbcs/lbc";
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException exception) {
            model.addAttribute("errors", List.of(exception.getMessage()));
            model.addAttribute("als", toDtoWithImages(alsService.findById(alsId)));
            model.addAttribute("lbc", lbc);
            model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
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
        try {
            LBC replacement = LBCDtoMapper.toDomain(
                    lbc, TypeLbFactory.forLBC(environment, lbc.getType()),
                    equipmentCatalogService::findById, sizeBounds);
            alsService.prepareForReplacement(source, replacement, lbc.isEquipmentProvided());
            source = alsService.replaceLBC(source.copy(), replacement);
            lbc = toDtoWithImage(replacement);
        } catch (ValidationSizeException exception) {
            model.addAttribute("errors", ValidationErrorFormatter.format(exception.getValidationResult()));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("errors", List.of(exception.getMessage()));
        }
        model.addAttribute("als", toDtoWithImages(source));
        model.addAttribute("lbc", lbc);
            model.addAttribute("equipmentList", equipmentCatalogService.findAll().stream()
                .map(EquipmentDtoMapper::toDto).toList());
        model.addAttribute("defaultEquipmentIds", equipmentCatalogService.findDefaultModelIds());
        return "lbcs/lbc";
    }

    /**
     * Заменяет текущий control-модуль на новый LBC.
     */
    /** Преобразует LBC в LC и отдельный LB в каталожной конфигурации ALS. */
    @PostMapping("/{alsId}/replaceLC")
    public String replaceLBCWithLC(@PathVariable Long alsId) {
        ALS saved = alsService.replaceWithNewLCandSaveALS(alsId);
        return "redirect:/alss/" + saved.getId();
    }

    @PostMapping("/{alsId}/replaceLBC")
    public String replaceLBC(@PathVariable Long alsId) {
        com.lb_calc_web.domain.model.ALS saved = alsService.replaceWithNewLBCandSaveALS(alsId);
        return "redirect:/alss/" + saved.getId();
    }

    /** Удаляет выбранный экземпляр LB из редактируемой конфигурации ALS. */
    @PostMapping("/{alsId}/lbs/{lbId}/delete")
    public String deleteLB(
            @PathVariable Long alsId,
            @PathVariable Long lbId
    ) {
        ALSDTO als = ALSDtoMapper.toDto(alsService.deleteLBandSaveALS(alsId, lbId));
        return "redirect:/alss/" + als.getId();
    }
}
