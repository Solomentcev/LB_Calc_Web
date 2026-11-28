package com.lb_calc_web.domain.equipment;

import java.util.Set;

/** Физическая категория модели оборудования. */
public enum EquipmentCategory {
    DISPLAY(EquipmentFunctionalType.DISPLAY,
            DisplayPurpose.INCH_10,
            DisplayPurpose.INCH_17,
            DisplayPurpose.INCH_19,
            DisplayPurpose.CUSTOM),
    BAR_READER(EquipmentFunctionalType.ACCESS,
            AccessPurpose.BAR_CODE,
            AccessPurpose.QR_CODE),
    RFID_READER(EquipmentFunctionalType.ACCESS,
            AccessPurpose.RFID),
    PAYMENT_TERMINAL(EquipmentFunctionalType.PAYMENT,
            PaymentPurpose.CARDS,
            PaymentPurpose.CARDS_AND_CASH),
    CASH_ACCEPTOR(EquipmentFunctionalType.PAYMENT,
            PaymentPurpose.CASH,
            PaymentPurpose.CARDS_AND_CASH),
    PRINTER(EquipmentFunctionalType.PRINTER,
            PrinterPurpose.PRESENT,
            PrinterPurpose.OTHER),
    COMPUTER(EquipmentFunctionalType.SYSTEM,
            SystemPurpose.COMPUTER),
    LOCK_CONTROL(EquipmentFunctionalType.SYSTEM,
            SystemPurpose.LOCK_CONTROL),
    POWER_SUPPLY(EquipmentFunctionalType.SYSTEM,
            SystemPurpose.POWER_SUPPLY),
    BACKUP_POWER(EquipmentFunctionalType.SYSTEM,
            SystemPurpose.BACKUP_POWER);

    private final EquipmentFunctionalType functionalType;
    private final Set<EquipmentPurpose> supportedPurposes;

    EquipmentCategory(
            EquipmentFunctionalType functionalType,
            EquipmentPurpose... supportedPurposes
    ) {
        this.functionalType = functionalType;
        this.supportedPurposes = Set.of(supportedPurposes);
    }

    public EquipmentFunctionalType getFunctionalType() {
        return functionalType;
    }

    /** Проверяет допустимость назначения для физической категории. */
    public boolean supports(EquipmentPurpose purpose) {
        return functionalType.supports(purpose) && supportedPurposes.contains(purpose);
    }
}
