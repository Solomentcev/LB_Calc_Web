package com.lb_calc_web.domain.equipment;

/** Функциональные области оборудования управляющего модуля. */
public enum EquipmentFunctionalType {
    DISPLAY(DisplayPurpose.class),
    ACCESS(AccessPurpose.class),
    PAYMENT(PaymentPurpose.class),
    PRINTER(PrinterPurpose.class),
    SYSTEM(SystemPurpose.class);

    private final Class<? extends Enum<?>> purposeType;

    EquipmentFunctionalType(Class<? extends Enum<?>> purposeType) {
        this.purposeType = purposeType;
    }

    public Class<? extends Enum<?>> getPurposeType() {
        return purposeType;
    }

    public boolean supports(EquipmentPurpose purpose) {
        return purpose != null
                && purpose.getFunctionalType() == this
                && purposeType.isInstance(purpose);
    }
}
