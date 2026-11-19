package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.attributes.PositionLC;
import com.lb_calc_web.service.EquipmentCatalogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.ui.Model;

public abstract class BaseCatalogController {

    private final EquipmentCatalogService equipmentCatalogService;

    protected BaseCatalogController(
            EquipmentCatalogService equipmentCatalogService
    ) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    /**
     * Цвета корпуса и дверей.
     */
    protected final List<Colors> colorsList =
            Arrays.asList(Colors.values());

    /**
     * Положение модуля управления внутри ALS.
     */
    protected final List<PositionControlModule>
            positionControlModuleList =
            Arrays.asList(PositionControlModule.values());

    /**
     * Старое имя оставлено для совместимости старых шаблонов.
     */
    @Deprecated
    protected final List<PositionLC> positionLCList =
            Arrays.asList(PositionLC.values());

    /**
     * Способы доступа LBC.
     */
    protected final List<AccessMethod> accessMethodList =
            Arrays.asList(AccessMethod.values());

    /**
     * Варианты печати LBC.
     */
    protected final List<PrintOption> printOptionList =
            Arrays.asList(PrintOption.values());

    /**
     * Способы оплаты.
     */
    protected final List<Payment> paymentList =
            Arrays.asList(Payment.values());

    /**
     * Список типов LB из конфигурации.
     *
     * <p>TypeLb больше не enum. Типы задаются в size-bounds.properties.</p>
     */
    @Value("${lb.types}")
    private String lbTypes;

    /**
     * Направление открытия дверей.
     */
    protected final List<DirectionDoorOpening>
            directionDoorOpeningList =
            Arrays.asList(DirectionDoorOpening.values());

    @ModelAttribute("colorsList")
    public List<Colors> colorsList() {
        return colorsList;
    }

    @ModelAttribute("positionControlModuleList")
    public List<PositionControlModule>
    positionControlModuleList() {
        return positionControlModuleList;
    }

    @Deprecated
    @ModelAttribute("positionLCList")
    public List<PositionLC> positionLCList() {
        return positionLCList;
    }

    @ModelAttribute("accessMethodList")
    public List<AccessMethod> accessMethodList() {
        return accessMethodList;
    }

    @ModelAttribute("printOptionList")
    public List<PrintOption> printOptionList() {
        return printOptionList;
    }

    @ModelAttribute("paymentList")
    public List<Payment> paymentList() {
        return paymentList;
    }

    @ModelAttribute("displayList")
    public List<String> displayList() {
        return equipmentCatalogService.findActiveNamesByCategory("DISPLAY");
    }

    @ModelAttribute("barReaderList")
    public List<String> barReaderList() {
        return equipmentCatalogService.findActiveNamesByCategory("BAR_READER");
    }

    /**
     * Добавляет в варианты выбора текущие модели, если они были деактивированы.
     * Это позволяет сохранить историческую конфигурацию без разрешения выбрать
     * деактивированную модель для новой конфигурации.
     */
    protected void retainSelectedEquipmentOptions(
            Model model,
            String display,
            String barReader
    ) {
        retainSelectedOption(model, "displayList", display);
        retainSelectedOption(model, "barReaderList", barReader);
    }

    private void retainSelectedOption(Model model, String attribute, String selectedValue) {
        if (selectedValue == null || selectedValue.isBlank()) {
            return;
        }

        Object currentOptions = model.asMap().get(attribute);
        List<String> options = new ArrayList<>();
        if (currentOptions instanceof List<?> values) {
            values.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .forEach(options::add);
        }
        if (!options.contains(selectedValue)) {
            options.add(selectedValue);
        }
        model.addAttribute(attribute, List.copyOf(options));
    }

    /**
     * Список типов LB.
     */
    @ModelAttribute("typeList")
    public List<String> typeList() {
        return getTypeLbList();
    }

    /**
     * Список типов LB.
     */
    @ModelAttribute("typeLbList")
    public List<String> typeLbList() {
        return getTypeLbList();
    }

    @ModelAttribute("directionDoorOpeningList")
    public List<DirectionDoorOpening>
    directionDoorOpeningList() {
        return directionDoorOpeningList;
    }

    private List<String> getTypeLbList() {
        if (lbTypes == null || lbTypes.isBlank()) {
            return List.of();
        }

        return Arrays.stream(lbTypes.split(","))
                .map(String::trim)
                .filter(type -> !type.isBlank())
                .toList();
    }
}