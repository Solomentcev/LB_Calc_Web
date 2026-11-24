package com.lb_calc_web.controller;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.attributes.PositionLC;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

/** Общие справочные значения для серверных представлений. */
public abstract class BaseCatalogController {

    protected final List<Colors> colorsList = Arrays.asList(Colors.values());
    protected final List<PositionControlModule> positionControlModuleList =
            Arrays.asList(PositionControlModule.values());

    @Deprecated
    protected final List<PositionLC> positionLCList = Arrays.asList(PositionLC.values());

    protected final List<AccessPurpose> accessMethodList = Arrays.asList(AccessPurpose.values());
    protected final List<PrinterPurpose> printOptionList = Arrays.asList(PrinterPurpose.values());
    protected final List<PaymentPurpose> paymentList = Arrays.asList(PaymentPurpose.values());
    protected final List<String> displayList =
            Arrays.stream(DisplayPurpose.values()).map(Enum::name).toList();
    protected final List<String> barReaderList = List.of(
            AccessPurpose.BAR_CODE.name(),
            AccessPurpose.QR_CODE.name(),
            AccessPurpose.RFID.name()
    );

    @Value("${lb.types}")
    private String lbTypes;

    protected final List<DirectionDoorOpening> directionDoorOpeningList =
            Arrays.asList(DirectionDoorOpening.values());

    @ModelAttribute("colorsList")
    public List<Colors> colorsList() {
        return colorsList;
    }

    @ModelAttribute("positionControlModuleList")
    public List<PositionControlModule> positionControlModuleList() {
        return positionControlModuleList;
    }

    @Deprecated
    @ModelAttribute("positionLCList")
    public List<PositionLC> positionLCList() {
        return positionLCList;
    }

    @ModelAttribute("accessMethodList")
    public List<AccessPurpose> accessMethodList() {
        return accessMethodList;
    }

    @ModelAttribute("printOptionList")
    public List<PrinterPurpose> printOptionList() {
        return printOptionList;
    }

    @ModelAttribute("paymentList")
    public List<PaymentPurpose> paymentList() {
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

    @ModelAttribute("typeList")
    public List<String> typeList() {
        return getTypeLbList();
    }

    @ModelAttribute("typeLbList")
    public List<String> typeLbList() {
        return getTypeLbList();
    }

    @ModelAttribute("directionDoorOpeningList")
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
