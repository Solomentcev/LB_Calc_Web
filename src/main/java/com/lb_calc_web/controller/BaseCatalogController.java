package com.lb_calc_web.controller;

import com.lb_calc_web.model.attributes.*;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

public abstract class BaseCatalogController {
    protected final List<Colors> colorsList = Arrays.asList(Colors.values());
    protected final List<PositionLC> positionLCList = Arrays.asList(PositionLC.values());
    protected final List<Payment> paymentList = Arrays.asList(Payment.values());
    protected final List<DisplayLC> displayList = Arrays.asList(DisplayLC.values());
    protected final List<BarReader> barReaderList = Arrays.asList(BarReader.values());
    protected final List<TypeLb> typeLbList = Arrays.asList(TypeLb.values());
    protected final List<DirectionDoorOpening> directionDoorOpeningList = Arrays.asList(DirectionDoorOpening.values());

    @ModelAttribute("colorsList")
    public List<Colors> colorsList() { return colorsList; }

    @ModelAttribute("positionLCList")
    public List<PositionLC> positionLCList() { return positionLCList; }

    @ModelAttribute("paymentList")
    public List<Payment> paymentList() { return paymentList; }

    @ModelAttribute("displayList")
    public List<DisplayLC> displayList() { return displayList; }

    @ModelAttribute("barReaderList")
    public List<BarReader> barReaderList() { return barReaderList; }

    @ModelAttribute("typeList")
    public List<TypeLb> typeList() { return typeLbList; }

    @ModelAttribute("typeLbList")
    public List<TypeLb> typeLbList() { return typeLbList; }

    @ModelAttribute("directionDoorOpeningList")
    public List<DirectionDoorOpening> directionDoorOpeningList() { return directionDoorOpeningList; }
}