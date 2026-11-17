package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentItem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Общий контракт доменных модулей управления.
 */
public interface ControlModule {

    /**
     * Возвращает независимую копию конфигурации управления.
     *
     * @return копия конфигурации, изменение которой не влияет на модуль
     */
    ControlConfiguration getControlConfiguration();

    /**
     * Формирует текстовое описание функциональной конфигурации
     * управляющего модуля.
     *
     * <p>В описании отражается физическое количество каждой позиции
     * оборудования, поскольку количество является частью комплектации.</p>
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

        String equipment = configuration.getEquipmentConfiguration()
                .getAllItems()
                .stream()
                .map(ControlModule::formatEquipmentItem)
                .collect(Collectors.joining(", "));

        return "Способы доступа: " + accessMethods + ";\n"
                + "Печать: " + printOptions + ";\n"
                + "Оплата: " + configuration.getPayment() + ";\n"
                + "Оборудование: " + equipment;
    }

    /**
     * Форматирует позицию оборудования с учётом её количества.
     *
     * @param item позиция оборудования
     * @return наименование и количество, если оно больше единицы
     */
    private static String formatEquipmentItem(EquipmentItem item) {
        String name = item.equipment().getName();

        return item.quantity() == 1
                ? name
                : name + " (" + item.quantity() + " шт.)";
    }

    List<Equipment> getEquipment();

    void addEquipment(Equipment equipment);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}
