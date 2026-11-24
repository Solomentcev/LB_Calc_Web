package com.lb_calc_web.domain.equipment;

/** Способы доступа, поддерживаемые оборудованием. */
public enum AccessPurpose implements EquipmentPurpose {
    PIN,
    RFID,
    BAR_CODE,
    QR_CODE;

    @Override
    public EquipmentFunctionalType getFunctionalType() {
        return EquipmentFunctionalType.ACCESS;
    }
}
