package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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

    /**
     * Создаёт конфигурацию управления.
     *
     * @param display назначение дисплея
     * @param access назначение доступа
     * @param payment назначение оплаты
     * @param printer назначение принтера
     * @param equipment фактически установленное оборудование и его количество
     */
    public ControlConfiguration(
            DisplayPurpose display,
            AccessPurpose access,
            PaymentPurpose payment,
            PrinterPurpose printer,
            Map<Equipment, Integer> equipment
    ) {
        this.display = display;
        this.access = access;
        this.payment = payment;
        this.printer = printer;
        this.equipment = copyEquipment(equipment);
        validateEquipment();
    }

    private static Map<Equipment, Integer> copyEquipment(Map<Equipment, Integer> source) {
        Objects.requireNonNull(source, "Состав оборудования не должен быть null");
        Map<Equipment, Integer> result = new LinkedHashMap<>();
        source.forEach((item, quantity) -> {
            Equipment equipment = Objects.requireNonNull(item,
                    "Оборудование не должно быть null");
            Integer count = Objects.requireNonNull(quantity,
                    "Количество оборудования не должно быть null");
            if (count < 1) {
                throw new IllegalArgumentException(
                        "Количество оборудования должно быть не меньше 1");
            }
            result.merge(equipment, count, (first, second) -> {
                try {
                    return Math.addExact(first, second);
                } catch (ArithmeticException exception) {
                    throw new IllegalArgumentException(
                            "Количество оборудования превышает допустимый диапазон",
                            exception);
                }
            });
        });
        return result;
    }

    private void validateEquipment() {
        long displayCount = equipment.entrySet().stream()
                .filter(entry -> entry.getKey().isDisplay())
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

    /** @return неизменяемое представление состава оборудования */
    public Map<Equipment, Integer> getEquipmentQuantities() {
        return Map.copyOf(equipment);
    }

    /** @return оборудование в стабильном порядке по идентификатору */
    public List<Equipment> getAllEquipment() {
        return equipment.keySet().stream()
                .sorted(Comparator.comparing(Equipment::getId))
                .toList();
    }

    /**
     * Возвращает количество модели в конфигурации.
     *
     * @param item модель оборудования
     * @return количество или ноль, если модель не установлена
     */
    public int getQuantity(Equipment item) {
        return equipment.getOrDefault(
                Objects.requireNonNull(item, "Оборудование не должно быть null"), 0);
    }

    /**
     * Добавляет оборудование или увеличивает количество установленной модели.
     *
     * @param item модель оборудования
     * @param quantity добавляемое количество
     */
    public void addEquipment(Equipment item, int quantity) {
        Equipment equipmentItem = Objects.requireNonNull(item,
                "Оборудование не должно быть null");
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Количество оборудования должно быть не меньше 1");
        }
        int current = equipment.getOrDefault(equipmentItem, 0);
        final int updated;
        try {
            updated = Math.addExact(current, quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Количество оборудования превышает допустимый диапазон", exception);
        }
        if (equipmentItem.isDisplay()
                && (updated > 1 || equipment.keySet().stream()
                .anyMatch(existing -> existing.isDisplay() && !existing.equals(equipmentItem)))) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
        equipment.put(equipmentItem, updated);
    }

    /**
     * Удаляет модель оборудования из конфигурации.
     *
     * @param item удаляемая модель
     * @return true, если модель была установлена
     */
    public boolean removeEquipment(Equipment item) {
        return equipment.remove(
                Objects.requireNonNull(item, "Оборудование не должно быть null")) != null;
    }

    /** @return независимая копия конфигурации */
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
