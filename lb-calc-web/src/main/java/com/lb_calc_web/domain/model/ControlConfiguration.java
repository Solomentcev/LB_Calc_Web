package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.equipment.SystemPurpose;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Конфигурация функциональных возможностей и оборудования модуля управления.
 *
 * <p>Назначение каждой функциональной области хранится отдельно от состава
 * физически установленного оборудования.</p>
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
     * @param display выбранное назначение дисплея
     * @param access выбранное назначение доступа
     * @param payment выбранное назначение оплаты
     * @param printer выбранное назначение принтера
     * @param system выбранное системное назначение
     * @param equipment установленное оборудование и его количество
     */
    public ControlConfiguration(
            DisplayPurpose display,
            AccessPurpose access,
            PaymentPurpose payment,
            PrinterPurpose printer,
            SystemPurpose system,
            Map<Equipment, Integer> equipment
    ) {
        this.display = display;
        this.access = access;
        this.payment = payment;
        this.printer = printer;
        this.system = system;
        this.equipment = copyEquipment(equipment);
        validateEquipment();
    }

    /**
     * Создаёт конфигурацию из прежнего представления состава оборудования.
     *
     * <p>Конструктор оставлен для совместимости существующих DTO-мапперов
     * на время их последовательного перевода на новую модель.</p>
     *
     * @param configuration прежняя конфигурация оборудования
     */
    public ControlConfiguration(EquipmentConfiguration configuration) {
        this(null, null, null, null, null, toEquipmentMap(configuration));
    }

    private static Map<Equipment, Integer> toEquipmentMap(
            EquipmentConfiguration configuration
    ) {
        Objects.requireNonNull(
                configuration,
                "Конфигурация оборудования не должна быть null"
        );
        Map<Equipment, Integer> result = new LinkedHashMap<>();
        configuration.getAllItems().forEach(item ->
                result.put(item.equipment(), item.quantity()));
        return result;
    }

    private static Map<Equipment, Integer> copyEquipment(
            Map<Equipment, Integer> source
    ) {
        Objects.requireNonNull(source, "Состав оборудования не должен быть null");
        Map<Equipment, Integer> result = new LinkedHashMap<>();
        source.forEach((position, quantity) -> {
            Equipment equipment = Objects.requireNonNull(
                    position,
                    "Оборудование не должно быть null"
            );
            int count = Objects.requireNonNull(
                    quantity,
                    "Количество оборудования не должно быть null"
            );
            if (count < 1) {
                throw new IllegalArgumentException(
                        "Количество оборудования должно быть не меньше 1");
            }
            result.put(equipment, count);
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

    /** @return выбранное назначение дисплея или null */
    public DisplayPurpose getDisplay() {
        return display;
    }

    /** @return выбранное назначение доступа или null */
    public AccessPurpose getAccess() {
        return access;
    }

    /** @return выбранное назначение оплаты или null */
    public PaymentPurpose getPayment() {
        return payment;
    }

    /** @return выбранное назначение принтера или null */
    public PrinterPurpose getPrinter() {
        return printer;
    }

    /** @return выбранное системное назначение или null */
    public SystemPurpose getSystem() {
        return system;
    }

    /**
     * Возвращает независимую копию состава оборудования.
     *
     * @return оборудование и его количество
     */
    public Map<Equipment, Integer> getEquipmentQuantities() {
        return Map.copyOf(equipment);
    }

    /**
     * Возвращает позиции оборудования в стабильном порядке по ID каталога.
     *
     * @return неизменяемый список оборудования
     */
    public List<Equipment> getAllEquipment() {
        return equipment.keySet().stream()
                .sorted(Comparator.comparing(Equipment::getCatalogPositionId))
                .toList();
    }

    /**
     * Возвращает количество указанной позиции оборудования.
     *
     * @param position позиция оборудования
     * @return количество или ноль, если позиция отсутствует
     */
    public int getQuantity(Equipment position) {
        return equipment.getOrDefault(
                Objects.requireNonNull(position, "Оборудование не должно быть null"),
                0
        );
    }

    /**
     * Добавляет оборудование или увеличивает количество существующей позиции.
     *
     * @param position добавляемая позиция
     * @param quantity добавляемое количество
     */
    public void addEquipment(Equipment position, int quantity) {
        Equipment item = Objects.requireNonNull(
                position,
                "Оборудование не должно быть null"
        );
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Количество оборудования должно быть не меньше 1");
        }

        int newQuantity;
        try {
            newQuantity = Math.addExact(equipment.getOrDefault(item, 0), quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Количество оборудования превышает допустимый диапазон",
                    exception
            );
        }

        if (item.isDisplay() && newQuantity > 1) {
            throw new IllegalArgumentException(
                    "Количество дисплеев в модуле должно быть равно 1");
        }
        if (item.isDisplay() && equipment.keySet().stream()
                .anyMatch(existing -> existing.isDisplay() && !existing.equals(item))) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
        equipment.put(item, newQuantity);
    }

    /**
     * Удаляет позицию оборудования целиком.
     *
     * @param position удаляемая позиция
     * @return true, если позиция присутствовала в конфигурации
     */
    public boolean removeEquipment(Equipment position) {
        return equipment.remove(
                Objects.requireNonNull(position, "Оборудование не должно быть null")
        ) != null;
    }

    /**
     * Создаёт независимую копию конфигурации.
     *
     * @return копия конфигурации
     */
    public ControlConfiguration copy() {
        return new ControlConfiguration(
                display, access, payment, printer, system, equipment);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ControlConfiguration other)) {
            return false;
        }
        return display == other.display
                && access == other.access
                && payment == other.payment
                && printer == other.printer
                && system == other.system
                && equipment.equals(other.equipment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(display, access, payment, printer, system, equipment);
    }

    @Override
    public String toString() {
        return "ControlConfiguration{" +
                "display=" + display +
                ", access=" + access +
                ", payment=" + payment +
                ", printer=" + printer +
                ", system=" + system +
                ", equipment=" + equipment +
                '}';
    }
}
