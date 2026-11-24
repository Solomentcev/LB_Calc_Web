package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Общий контракт доменных модулей управления. */
public interface ControlModule {

    /** @return независимая копия конфигурации управления */
    ControlConfiguration getControlConfiguration();

    /** Заменяет полную конфигурацию управляющего модуля. */
    void setControlConfiguration(ControlConfiguration configuration);

    /** Заменяет функциональные назначения, сохраняя состав оборудования. */
    void setFunctionalConfiguration(FunctionalConfiguration configuration);

    /** Формирует описание назначений и фактически установленного оборудования. */
    default String getControlDescription() {
        ControlConfiguration configuration = getControlConfiguration();
        FunctionalConfiguration functional = configuration.getFunctionalConfiguration();
        String purposes = Stream.of(
                        functional.getDisplay().name(),
                        functional.getAccess().stream().sorted().map(Enum::name)
                                .collect(Collectors.joining("+")),
                        functional.getPayment().name(),
                        functional.getPrinter().name())
                .collect(Collectors.joining(", ", "[", "]"));

        String equipment = configuration.getEquipment().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(
                        Comparator.comparing(
                                Equipment::getId,
                                Comparator.nullsLast(Comparator.naturalOrder()))))
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
