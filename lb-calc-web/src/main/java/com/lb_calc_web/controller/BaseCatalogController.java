package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

/** Общие справочные значения для серверных представлений. */
public abstract class BaseCatalogController {

    protected final List<Colors> colorsList = Arrays.asList(Colors.values());
    protected final List<PositionControlModule> positionControlModuleList =
            Arrays.asList(PositionControlModule.values());

    @Value("${lb.types}")
    private String lbTypes;

    protected final List<DirectionDoorOpening> directionDoorOpeningList =
            Arrays.asList(DirectionDoorOpening.values());

    @ModelAttribute("colorsList")
    /** Возвращает справочник цветов для выбора в формах каталога. */
    public List<Colors> colorsList() {
        return colorsList;
    }

    @ModelAttribute("positionControlModuleList")
    /** Возвращает допустимые положения управляющего модуля. */
    public List<PositionControlModule> positionControlModuleList() {
        return positionControlModuleList;
    }

    @ModelAttribute("typeList")
    /** Возвращает настроенный список конструктивных типов модулей хранения. */
    public List<String> typeList() {
        return getTypeLbList();
    }

    @ModelAttribute("typeLbList")
    /** Возвращает типы LB, разрешённые конфигурацией приложения. */
    public List<String> typeLbList() {
        return getTypeLbList();
    }

    @ModelAttribute("directionDoorOpeningList")
    /** Возвращает допустимые направления открывания дверей. */
    public List<DirectionDoorOpening> directionDoorOpeningList() {
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
