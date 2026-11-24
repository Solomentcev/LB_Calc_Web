package com.lb_calc_web.domain.equipment;

/** Физическая категория каталожного оборудования. */
public enum EquipmentCategory {
    DISPLAY(EquipmentFunctionalType.DISPLAY),
    BAR_READER(EquipmentFunctionalType.ACCESS),
    RFID_READER(EquipmentFunctionalType.ACCESS),
    PAYMENT_TERMINAL(EquipmentFunctionalType.PAYMENT),
    CASH_ACCEPTOR(EquipmentFunctionalType.PAYMENT),
    PRINTER(EquipmentFunctionalType.PRINTER),
    COMPUTER(EquipmentFunctionalType.SYSTEM),
    LOCK_CONTROLLER(EquipmentFunctionalType.SYSTEM),
    POWER_SUPPLY(EquipmentFunctionalType.SYSTEM),
    UPS(EquipmentFunctionalType.SYSTEM),
    MODEM(EquipmentFunctionalType.SYSTEM),
    ROUTER(EquipmentFunctionalType.SYSTEM),
    ANTENNA(EquipmentFunctionalType.SYSTEM),
    OTHER(EquipmentFunctionalType.SYSTEM);

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
            case DISPLAY -> purpose instanceof DisplayPurpose displayPurpose
                    && displayPurpose != DisplayPurpose.NONE;
            case BAR_READER -> purpose == AccessPurpose.BAR_CODE
                    || purpose == AccessPurpose.QR_CODE;
            case RFID_READER -> purpose == AccessPurpose.RFID;
            case PAYMENT_TERMINAL -> purpose == PaymentPurpose.CARDS;
            case CASH_ACCEPTOR -> purpose == PaymentPurpose.CASH;
            case PRINTER -> purpose == PrinterPurpose.PRESENT;
            case COMPUTER -> purpose == SystemPurpose.COMPUTER;
            case LOCK_CONTROLLER -> purpose == SystemPurpose.LOCK_CONTROL;
            case POWER_SUPPLY -> purpose == SystemPurpose.POWER_SUPPLY;
            case UPS -> purpose == SystemPurpose.BACKUP_POWER;
            case MODEM, ROUTER, ANTENNA, OTHER -> purpose == SystemPurpose.OTHER;
        };
    }
}
