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
     * @return копия конфигурации
     */
    ControlConfiguration getControlConfiguration();

    /**
     * Формирует описание функций и установленного оборудования.
     *
     * <p>Функции определяются назначениями оборудования, поэтому
     * отдельные признаки доступа, оплаты и печати не дублируются.</p>
     *
     * @return описание конфигурации управления
     */
    default String getControlDescription() {
        ControlConfiguration configuration = getControlConfiguration();

        String purposes = configuration.getAllEquipment().stream()
                .map(equipment -> equipment.getPurpose().name())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));

        String equipment = configuration.getEquipmentConfiguration()
                .getAllItems()
                .stream()
                .map(ControlModule::formatEquipmentItem)
                .collect(Collectors.joining(", "));

        return "Функции: " + purposes + ";\n"
                + "Оборудование: " + equipment;
    }

    /**
     * Форматирует позицию оборудования с учётом количества.
     *
     * @param item позиция оборудования
     * @return наименование и количество
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
