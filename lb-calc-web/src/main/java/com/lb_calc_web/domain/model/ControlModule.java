package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;

public interface ControlModule {

    /**
     * Возвращает независимую копию конфигурации управления.
     *
     * @return копия конфигурации, изменение которой не влияет на модуль
     */
    ControlConfiguration getControlConfiguration();

    List<Equipment> getEquipment();

    void addEquipment(Equipment equipment);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}