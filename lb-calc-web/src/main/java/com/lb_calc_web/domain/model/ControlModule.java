package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;

public interface ControlModule {

    ControlConfiguration getControlConfiguration();

    List<Equipment> getEquipment();

    void addEquipment(Equipment equipment);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}