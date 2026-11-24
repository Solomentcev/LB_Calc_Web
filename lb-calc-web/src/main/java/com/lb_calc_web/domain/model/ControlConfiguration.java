package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Конфигурация функциональных возможностей и фактически установленного
 * оборудования модуля управления.
 *
 * <p>Назначения функциональных областей независимы от состава оборудования:
 * функция может быть предусмотрена конфигурацией без установленного
 * устройства, которое её реализует.</p>
 */
public class ControlConfiguration {

    private final DisplayPurpose display;
    private final AccessPurpose access;
    private final PaymentPurpose payment;
    private final PrinterPurpose printer;
    private final Map<Equipment, Integer> equipment;

    public ControlConfiguration(
            DisplayPurpose display,
            AccessPurpose access,
            PaymentPurpose payment,
            PrinterPurpose printer,
            Map<Equipment, Integer> equipment
    ) {
        this.display = Objects.requireNonNull(display, "Назначение дисплея не должно быть null");
        this.access = Objects.requireNonNull(access, "Назначение доступа не должно быть null");
        this.payment = Objects.requireNonNull(payment, "Назначение оплаты не должно быть null");
        this.printer = Objects.requireNonNull(printer, "Назначение принтера не должно быть null");
        this.equipment = copyEquipment(equipment);
        validateEquipment();
    }

    private static Map<Equipment, Integer> copyEquipment(Map<Equipment, Integer> source) {
        Objects.requireNonNull(source, "Состав оборудования не должен быть null");
        Map<Equipment, Integer> result = new LinkedHashMap<>();
        source.forEach((item, quantity) -> {
            Equipment model = Objects.requireNonNull(item, "Оборудование не должно быть null");
            Integer count = Objects.requireNonNull(quantity, "Количество оборудования не должно быть null");
            if (count < 1) {
                throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
            }
            result.merge(model, count, (first, second) -> {
                try {
                    return Math.addExact(first, second);
                } catch (ArithmeticException exception) {
                    throw new IllegalArgumentException(
                            "Количество оборудования превышает допустимый диапазон", exception);
                }
            });
        });
        return result;
    }

    private void validateEquipment() {
        long displayCount = equipment.entrySet().stream()
                .filter(entry -> entry.getKey().getCategory() == EquipmentCategory.DISPLAY)
                .mapToLong(Map.Entry::getValue)
                .sum();
        if (displayCount > 1) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
    }

    public DisplayPurpose getDisplay() { return display; }
    public AccessPurpose getAccess() { return access; }
    public PaymentPurpose getPayment() { return payment; }
    public PrinterPurpose getPrinter() { return printer; }

    /** @return неизменяемое соответствие моделей оборудования и их количества */
    public Map<Equipment, Integer> getEquipment() {
        return Map.copyOf(equipment);
    }

    public int getQuantity(Equipment item) {
        return equipment.getOrDefault(
                Objects.requireNonNull(item, "Оборудование не должно быть null"), 0);
    }

    public void addEquipment(Equipment item, int quantity) {
        Equipment model = Objects.requireNonNull(item, "Оборудование не должно быть null");
        if (quantity < 1) {
            throw new IllegalArgumentException("Количество оборудования должно быть не меньше 1");
        }

        int updated;
        try {
            updated = Math.addExact(equipment.getOrDefault(model, 0), quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Количество оборудования превышает допустимый диапазон", exception);
        }

        if (model.getCategory() == EquipmentCategory.DISPLAY
                && (updated > 1 || equipment.keySet().stream().anyMatch(existing ->
                existing.getCategory() == EquipmentCategory.DISPLAY && !existing.equals(model)))) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
        equipment.put(model, updated);
    }

    public boolean removeEquipment(Equipment item) {
        return equipment.remove(
                Objects.requireNonNull(item, "Оборудование не должно быть null")) != null;
    }

    public ControlConfiguration copy() {
        return new ControlConfiguration(display, access, payment, printer, equipment);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ControlConfiguration other)) return false;
        return display == other.display
                && access == other.access
                && payment == other.payment
                && printer == other.printer
                && equipment.equals(other.equipment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(display, access, payment, printer, equipment);
    }

    @Override
    public String toString() {
        return "ControlConfiguration{" +
                "display=" + display +
                ", access=" + access +
                ", payment=" + payment +
                ", printer=" + printer +
                ", equipment=" + equipment +
                '}';
    }
}
