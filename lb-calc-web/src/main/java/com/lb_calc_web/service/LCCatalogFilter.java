package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

/** Параметры фильтрации каталога модулей управления LC. */
public record LCCatalogFilter(
        Long id, String name,
        Integer heightMin, Integer heightMax,
        Integer widthMin, Integer widthMax,
        Integer depthMin, Integer depthMax,
        DisplayPurpose displayPurpose, AccessPurpose accessPurpose,
        PaymentPurpose paymentPurpose, PrinterPurpose printerPurpose,
        Colors colorBody, Colors colorDoor
) {
}
