package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;
import java.util.stream.Collectors;

public interface ControlModule {

    /**
     * Возвращает независимую копию конфигурации управления.
     *
     * @return копия конфигурации, изменение которой не влияет на модуль
     */
    ControlConfiguration getControlConfiguration();

    /**
     * Формирует общее текстовое описание функциональной конфигурации
     * управляющего модуля.
     *
     * @return описание способов доступа, печати, оплаты и оборудования
     */
    default String getControlDescription() {
        ControlConfiguration configuration = getControlConfiguration();

        String accessMethods = configuration.getAccessMethods().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ", "[", "]"));

        String printOptions = configuration.getPrintOptions().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ", "[", "]"));

        String equipment = getEquipment().stream()
                .map(Equipment::getName)
                .collect(Collectors.joining(", "));

        return "Способы доступа: " + accessMethods + ";\n"
                + "Печать: " + printOptions + ";\n"
                + "Оплата: " + configuration.getPayment() + ";\n"
                + "Оборудование: " + equipment;
    }

    List<Equipment> getEquipment();

    void addEquipment(Equipment equipment);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}