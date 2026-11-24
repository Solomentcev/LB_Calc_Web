package com.lb_calc_web.domain.equipment;

/** Функциональные типы оборудования модуля управления. */
public enum EquipmentFunctionalType {

    DISPLAY(DisplayPurpose.class),
    ACCESS(AccessPurpose.class),
    PAYMENT(PaymentPurpose.class),
    PRINTER(PrinterPurpose.class);

    private final Class<? extends Enum<?>> purposeType;

    EquipmentFunctionalType(Class<? extends Enum<?>> purposeType) {
        this.purposeType = purposeType;
    }

    public Class<? extends Enum<?>> getPurposeType() {
        return purposeType;
    }
}
