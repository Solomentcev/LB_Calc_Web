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

    /** Проверяет, относится ли назначение к этой функциональной области. */
    public boolean supports(EquipmentPurpose purpose) {
        return purpose != null
                && purpose.getFunctionalType() == this
                && purposeType.isInstance(purpose);
    }

    /** Восстанавливает типизированное назначение по его сохранённому имени. */
    public EquipmentPurpose parsePurpose(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Назначение оборудования не должно быть пустым");
        }

        for (Enum<?> constant : purposeType.getEnumConstants()) {
            if (constant.name().equals(value) && constant instanceof EquipmentPurpose purpose) {
                return purpose;
            }
        }

        throw new IllegalArgumentException(
                "Назначение " + value + " не поддерживается функциональным типом " + this);
    }
}
