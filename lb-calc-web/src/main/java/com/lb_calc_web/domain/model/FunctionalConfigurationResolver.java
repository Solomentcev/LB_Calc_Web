package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.EquipmentPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Согласует функциональные назначения с изменениями состава оборудования.
 * Пересчитывает только функциональные области, состав которых изменился.
 */
public final class FunctionalConfigurationResolver {

    /**
     * Сохраняет назначения неизменившихся областей и пересчитывает только изменённые.
     *
     * @param current текущая функциональная конфигурация
     * @param previousEquipment состав оборудования до изменения
     * @param updatedEquipment состав оборудования после изменения
     * @return согласованная функциональная конфигурация
     */
    public FunctionalConfiguration resolve(
            FunctionalConfiguration current,
            Map<Equipment, Integer> previousEquipment,
            Map<Equipment, Integer> updatedEquipment
    ) {
        Objects.requireNonNull(current, "Текущая функциональная конфигурация не должна быть null");
        Objects.requireNonNull(previousEquipment, "Предыдущий состав оборудования не должен быть null");
        Objects.requireNonNull(updatedEquipment, "Новый состав оборудования не должен быть null");

        boolean displayChanged = hasChanged(
                EquipmentFunctionalType.DISPLAY, previousEquipment, updatedEquipment);
        boolean accessChanged = hasChanged(
                EquipmentFunctionalType.ACCESS, previousEquipment, updatedEquipment);
        boolean paymentChanged = hasChanged(
                EquipmentFunctionalType.PAYMENT, previousEquipment, updatedEquipment);
        boolean printerChanged = hasChanged(
                EquipmentFunctionalType.PRINTER, previousEquipment, updatedEquipment);

        return new FunctionalConfiguration(
                displayChanged ? resolveDisplay(updatedEquipment) : current.getDisplay(),
                accessChanged ? resolveAccess(updatedEquipment) : current.getAccess(),
                paymentChanged ? resolvePayment(updatedEquipment) : current.getPayment(),
                printerChanged ? resolvePrinter(updatedEquipment) : current.getPrinter()
        );
    }

    /**
     * Нормализует функциональные назначения с учётом зависимостей между ними.
     * PIN использует дисплей и при отсутствии выбранного дисплея требует модель 10 дюймов.
     */
    public FunctionalConfiguration normalize(FunctionalConfiguration configuration) {
        Objects.requireNonNull(configuration, "Функциональная конфигурация не должна быть null");
        if (configuration.getAccess().contains(AccessPurpose.PIN)
                && configuration.getDisplay() == DisplayPurpose.NONE) {
            return new FunctionalConfiguration(
                    DisplayPurpose.INCH_10,
                    configuration.getAccess(),
                    configuration.getPayment(),
                    configuration.getPrinter()
            );
        }
        return configuration;
    }

    /**
     * Выполняет обратное преобразование функциональных назначений в требования
     * к физическому оборудованию. Конкретные модели выбираются из каталога сервисным слоем.
     *
     * @param configuration функциональные назначения управляющего модуля
     * @return требования, сгруппированные по функциональному типу оборудования
     */
    public Map<EquipmentFunctionalType, Set<EquipmentPurpose>> resolveRequiredEquipment(
            FunctionalConfiguration configuration
    ) {
        FunctionalConfiguration normalized = normalize(configuration);
        Map<EquipmentFunctionalType, Set<EquipmentPurpose>> requirements = new LinkedHashMap<>();

        if (normalized.getDisplay() != DisplayPurpose.NONE) {
            addRequirement(requirements, EquipmentFunctionalType.DISPLAY, normalized.getDisplay());
        }
        normalized.getAccess().stream()
                .filter(purpose -> purpose != AccessPurpose.PIN)
                .sorted()
                .forEach(purpose -> addRequirement(
                        requirements, EquipmentFunctionalType.ACCESS, purpose));

        PaymentPurpose payment = normalized.getPayment();
        if (payment == PaymentPurpose.CARDS || payment == PaymentPurpose.CARDS_AND_CASH) {
            addRequirement(requirements, EquipmentFunctionalType.PAYMENT, PaymentPurpose.CARDS);
        }
        if (payment == PaymentPurpose.CASH || payment == PaymentPurpose.CARDS_AND_CASH) {
            addRequirement(requirements, EquipmentFunctionalType.PAYMENT, PaymentPurpose.CASH);
        }
        if (normalized.getPrinter() != PrinterPurpose.ABSENT) {
            addRequirement(requirements, EquipmentFunctionalType.PRINTER, normalized.getPrinter());
        }

        Map<EquipmentFunctionalType, Set<EquipmentPurpose>> result = new LinkedHashMap<>();
        requirements.forEach((type, purposes) -> result.put(type, Set.copyOf(purposes)));
        return Map.copyOf(result);
    }

    /** Добавляет требование к физическому оборудованию. */
    private void addRequirement(
            Map<EquipmentFunctionalType, Set<EquipmentPurpose>> requirements,
            EquipmentFunctionalType type,
            EquipmentPurpose purpose
    ) {
        requirements.computeIfAbsent(type, ignored -> new LinkedHashSet<>()).add(purpose);
    }

    /** Проверяет, изменился ли состав оборудования заданной функциональной области. */
    private boolean hasChanged(EquipmentFunctionalType type,
                               Map<Equipment, Integer> previous,
                               Map<Equipment, Integer> updated) {
        return !equipmentOfType(previous, type).equals(equipmentOfType(updated, type));
    }

    /** Выбирает модели оборудования заданного функционального типа. */
    private Map<Equipment, Integer> equipmentOfType(
            Map<Equipment, Integer> equipment, EquipmentFunctionalType type) {
        return equipment.entrySet().stream()
                .filter(entry -> entry.getKey().getFunctionalType() == type)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** Определяет назначение дисплея по установленному оборудованию. */
    private DisplayPurpose resolveDisplay(Map<Equipment, Integer> equipment) {
        return equipment.keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.DISPLAY)
                .map(Equipment::getPurpose).map(DisplayPurpose.class::cast)
                .findFirst().orElse(DisplayPurpose.NONE);
    }

    /** Определяет набор назначений доступа по установленным считывателям. */
    private Set<AccessPurpose> resolveAccess(Map<Equipment, Integer> equipment) {
        EnumSet<AccessPurpose> purposes = EnumSet.noneOf(AccessPurpose.class);
        equipment.keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.ACCESS)
                .map(Equipment::getPurpose).map(AccessPurpose.class::cast)
                .forEach(purposes::add);
        return purposes;
    }

    /** Объединяет возможности установленных платёжных устройств. */
    private PaymentPurpose resolvePayment(Map<Equipment, Integer> equipment) {
        boolean cards = false;
        boolean cash = false;
        for (Equipment item : equipment.keySet()) {
            if (item.getFunctionalType() != EquipmentFunctionalType.PAYMENT) continue;
            PaymentPurpose purpose = (PaymentPurpose) item.getPurpose();
            cards |= purpose == PaymentPurpose.CARDS || purpose == PaymentPurpose.CARDS_AND_CASH;
            cash |= purpose == PaymentPurpose.CASH || purpose == PaymentPurpose.CARDS_AND_CASH;
        }
        if (cards && cash) return PaymentPurpose.CARDS_AND_CASH;
        if (cards) return PaymentPurpose.CARDS;
        if (cash) return PaymentPurpose.CASH;
        return PaymentPurpose.NONE;
    }

    /** Определяет наличие и тип установленного принтера. */
    private PrinterPurpose resolvePrinter(Map<Equipment, Integer> equipment) {
        boolean present = false;
        boolean other = false;
        for (Equipment item : equipment.keySet()) {
            if (item.getFunctionalType() != EquipmentFunctionalType.PRINTER) continue;
            present = true;
            other |= item.getPurpose() == PrinterPurpose.OTHER;
        }
        if (!present) return PrinterPurpose.ABSENT;
        return other ? PrinterPurpose.OTHER : PrinterPurpose.PRESENT;
    }
}
