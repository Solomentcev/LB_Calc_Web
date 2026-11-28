package com.lb_calc_web.domain.equipment;

/** Назначение оборудования для проведения платежей. */
public enum PaymentPurpose implements EquipmentPurpose {
    NONE,
    CARDS,
    CASH,
    CARDS_AND_CASH
}
