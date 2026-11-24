package com.lb_calc_web.domain.equipment;

/** Назначение системного оборудования управляющего модуля. */
public enum SystemPurpose implements EquipmentPurpose {
    COMPUTER,
    LOCK_CONTROL,
    POWER_SUPPLY,
    BACKUP_POWER,
    OTHER;

    @Override
    public EquipmentFunctionalType getFunctionalType() {
        return EquipmentFunctionalType.SYSTEM;
    }
}
