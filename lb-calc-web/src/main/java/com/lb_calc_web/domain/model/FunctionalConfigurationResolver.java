package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Согласует функциональные назначения с изменениями состава оборудования.
 * Пересчитывает только функциональные области, состав которых изменился.
 */
public final class FunctionalConfigurationResolver {

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

    private boolean hasChanged(EquipmentFunctionalType type,
                               Map<Equipment, Integer> previous,
                               Map<Equipment, Integer> updated) {
        return !equipmentOfType(previous, type).equals(equipmentOfType(updated, type));
    }

    private Map<Equipment, Integer> equipmentOfType(
            Map<Equipment, Integer> equipment, EquipmentFunctionalType type) {
        return equipment.entrySet().stream()
                .filter(entry -> entry.getKey().getFunctionalType() == type)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private DisplayPurpose resolveDisplay(Map<Equipment, Integer> equipment) {
        return equipment.keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.DISPLAY)
                .map(Equipment::getPurpose).map(DisplayPurpose.class::cast)
                .findFirst().orElse(DisplayPurpose.NONE);
    }

    private Set<AccessPurpose> resolveAccess(Map<Equipment, Integer> equipment) {
        EnumSet<AccessPurpose> purposes = EnumSet.noneOf(AccessPurpose.class);
        equipment.keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.ACCESS)
                .map(Equipment::getPurpose).map(AccessPurpose.class::cast)
                .forEach(purposes::add);
        return purposes;
    }

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
