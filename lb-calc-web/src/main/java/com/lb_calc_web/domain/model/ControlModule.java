package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Общий контракт доменных модулей управления. */
public interface ControlModule {

    /** @return независимая копия конфигурации управления */
    ControlConfiguration getControlConfiguration();

    /** Формирует описание назначений и фактически установленного оборудования. */
    default String getControlDescription() {
        ControlConfiguration configuration = getControlConfiguration();
        String purposes = Stream.of(
                        configuration.getDisplay(),
                        configuration.getAccess(),
                        configuration.getPayment(),
                        configuration.getPrinter())
                .filter(Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.joining(", ", "[", "]"));

        String equipment = configuration.getEquipment().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        java.util.Comparator.comparing(
                                Equipment::getId,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))))
                .map(ControlModule::formatEquipment)
                .collect(Collectors.joining(", "));

        return "Функции: " + purposes + ";\n"
                + "Оборудование: " + equipment;
    }

    private static String formatEquipment(Map.Entry<Equipment, Integer> entry) {
        String name = entry.getKey().getName();
        return entry.getValue() == 1 ? name : name + " (" + entry.getValue() + " шт.)";
    }

    Map<Equipment, Integer> getEquipment();

    void addEquipment(Equipment equipment, int quantity);

    void removeEquipment(Equipment equipment);

    int getEquipmentHeight();
}
