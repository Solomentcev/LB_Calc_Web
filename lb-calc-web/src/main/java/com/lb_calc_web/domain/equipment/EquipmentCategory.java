package com.lb_calc_web.domain.equipment;

/** Физическая категория модели оборудования. */
public enum EquipmentCategory {
    DISPLAY(EquipmentFunctionalType.DISPLAY),
    BAR_READER(EquipmentFunctionalType.ACCESS),
    RFID_READER(EquipmentFunctionalType.ACCESS),
    PAYMENT_TERMINAL(EquipmentFunctionalType.PAYMENT),
    CASH_ACCEPTOR(EquipmentFunctionalType.PAYMENT),
    PRINTER(EquipmentFunctionalType.PRINTER);

    private final EquipmentFunctionalType functionalType;

    EquipmentCategory(EquipmentFunctionalType functionalType) {
        this.functionalType = functionalType;
    }

    public EquipmentFunctionalType getFunctionalType() {
        return functionalType;
    }

    /** Проверяет допустимость назначения для физической категории. */
    public boolean supports(EquipmentPurpose purpose) {
        if (purpose == null || !functionalType.supports(purpose)) {
            return false;
        }
        return switch (this) {
            case DISPLAY -> purpose instanceof DisplayPurpose;
            case BAR_READER -> purpose == AccessPurpose.BAR_CODE
                    || purpose == AccessPurpose.QR_CODE;
            case RFID_READER -> purpose == AccessPurpose.RFID;
            case PAYMENT_TERMINAL -> purpose == PaymentPurpose.CARDS
                    || purpose == PaymentPurpose.CARDS_AND_CASH;
            case CASH_ACCEPTOR -> purpose == PaymentPurpose.CASH
                    || purpose == PaymentPurpose.CARDS_AND_CASH;
            case PRINTER -> purpose == PrinterPurpose.PRESENT
                    || purpose == PrinterPurpose.OTHER;
        };
    }
}
