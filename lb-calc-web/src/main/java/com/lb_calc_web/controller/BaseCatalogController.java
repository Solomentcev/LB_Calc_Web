package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.attributes.PositionLC;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

public abstract class BaseCatalogController {

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
     * Способы оплаты.
     */
    protected final List<Payment> paymentList =
            Arrays.asList(Payment.values());

    /**
     * Доступные дисплеи LC.
     *
     * <p>Display больше не enum, поэтому передаём
     * в шаблон имена оборудования.</p>
     */
    protected final List<String> displayList =
            List.of(
                    Display.NONE.getName(),
                    Display.LC10.getName(),
                    Display.LC17.getName(),
                    Display.LC19.getName()
            );

    /**
     * Доступные сканеры штрихкода.
     *
     * <p>BarReader больше не enum.</p>
     */
    protected final List<String> barReaderList =
            List.of(
                    BarReader.NONE.getName(),
                    BarReader.READER_1D.getName(),
                    BarReader.READER_2D.getName()
            );

    /**
     * Список типов LB из конфигурации.
     *
     * <p>TypeLb больше не enum.
     * Типы задаются в size-bounds.properties.</p>
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

    @ModelAttribute("paymentList")
    public List<Payment> paymentList() {
        return paymentList;
    }

    @ModelAttribute("displayList")
    public List<String> displayList() {
        return displayList;
    }

    @ModelAttribute("barReaderList")
    public List<String> barReaderList() {
        return barReaderList;
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