package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.Map;

/** Общий контракт доменных модулей управления. */
public interface ControlModule {

    /** @return независимая копия конфигурации управления */
    ControlConfiguration getControlConfiguration();

    /** Заменяет полную конфигурацию управляющего модуля. */
    void setControlConfiguration(ControlConfiguration configuration);

    /** Заменяет функциональные назначения, сохраняя состав оборудования. */
    void setFunctionalConfiguration(FunctionalConfiguration configuration);

    /** Формирует описание функциональных назначений и установленного оборудования. */
    default String getControlDescription() {
        return ModuleTextFormatter.controlDetailsDescription(this);
    }

    Map<Equipment, Integer> getEquipment();

    void addEquipment(Equipment equipment, int quantity);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}
