package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Формирует описание функциональных назначений и установленного оборудования.
     *
     * @return описание конфигурации управления
     */
    default String getControlDescription() {
        ControlConfiguration configuration = getControlConfiguration();

        String purposes = Stream.of(
                        configuration.getDisplay(),
                        configuration.getAccess(),
                        configuration.getPayment(),
                        configuration.getPrinter(),
                        configuration.getSystem()
                )
                .filter(java.util.Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.joining(", ", "[", "]"));

        String equipment = configuration.getEquipmentQuantities().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        java.util.Comparator.comparing(Equipment::getCatalogPositionId)))
                .map(ControlModule::formatEquipment)
                .collect(Collectors.joining(", "));

        return "Функции: " + purposes + ";\n"
                + "Оборудование: " + equipment;
    }

    /**
     * Форматирует позицию оборудования с учётом количества.
     *
     * @param entry позиция оборудования и её количество
     * @return наименование и количество
     */
    private static String formatEquipment(Map.Entry<Equipment, Integer> entry) {
        String name = entry.getKey().getName();
        return entry.getValue() == 1
                ? name
                : name + " (" + entry.getValue() + " шт.)";
    }

    List<Equipment> getEquipment();

    void addEquipment(Equipment equipment);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}
