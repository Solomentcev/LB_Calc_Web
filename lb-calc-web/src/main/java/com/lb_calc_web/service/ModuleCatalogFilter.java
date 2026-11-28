package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

/** Общие параметры фильтрации списков ALS, LBC и LB. */
public record ModuleCatalogFilter(
        Long id,
        String name,
        String type,
        Integer heightMin,
        Integer heightMax,
        Integer widthMin,
        Integer widthMax,
        Integer depthMin,
        Integer depthMax,
        Integer upperFrameMin,
        Integer upperFrameMax,
        Integer bottomFrameMin,
        Integer bottomFrameMax,
        Integer countCellsMin,
        Integer countCellsMax,
        PositionControlModule positionControlModule,
        DirectionDoorOpening directionDoorOpening,
        DisplayPurpose displayPurpose,
        AccessPurpose accessPurpose,
        PaymentPurpose paymentPurpose,
        PrinterPurpose printerPurpose,
        Colors colorBody,
        Colors colorDoor
) {
}
