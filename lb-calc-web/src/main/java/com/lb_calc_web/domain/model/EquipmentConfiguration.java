package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Конфигурация оборудования модуля управления.
 * Позиции идентифицируются ID каталога, количество хранится отдельно.
 */
public class EquipmentConfiguration {
    private final Map<Equipment, Integer> requiredEquipment;
    private final Map<Equipment, Integer> additionalEquipment;

    public EquipmentConfiguration(List<Equipment> requiredEquipment,
                                  List<Equipment> additionalEquipment) {
        this.requiredEquipment = copyEquipment(requiredEquipment, "Обязательное оборудование");
        this.additionalEquipment = copyEquipment(additionalEquipment, "Дополнительное оборудование");
        validateGroups();
    }

    private Map<Equipment, Integer> copyEquipment(List<Equipment> source, String listName) {
        Map<Equipment, Integer> result = new LinkedHashMap<>();
        if (source == null) return result;
        for (Equipment equipment : source) {
            Equipment position = Objects.requireNonNull(
                    equipment, listName + " не должно содержать null");
            result.merge(position, 1, EquipmentConfiguration::sumQuantities);
        }
        validateDisplayQuantity(result);
        return result;
    }

    private void validateDisplayQuantity(Map<Equipment, Integer> positions) {
        positions.forEach((position, quantity) -> {
            if (position isDisplay() && quantity > 1) {
                throw new IllegalArgumentException(
                        "Количество дисплеев в модуле должно быть равно 1");
            }
        });
    }

    private void validateGroups() {
        for (Equipment equipment : requiredEquipment.keySet()) {
            if (additionalEquipment.containsKey(equipment)) {
                throw new IllegalArgumentException(
                        "Позиция оборудования не может одновременно быть обязательной " +
                                "и дополнительной: " + equipment.getCatalogPositionId());
            }
        }

        validateSingleDisplay(requiredEquipment, additionalEquipment);
    }

    /**
     * Проверяет, что в конфигурации установлено не более одного дисплея.
     *
     * @param first первая группа оборудования
     * @param second вторая группа оборудования
     * @throws IllegalArgumentException если выбрано несколько разных дисплеев
     */
    private void validateSingleDisplay(
            Map<Equipment, Integer> first,
            Map<Equipment, Integer> second
    ) {
        long displayPositions = java.util.stream.Stream.concat(
                        first.keySet().stream(),
                        second.keySet().stream()
                )
                .filter(Equipment::isDisplay)
                .count();

        if (displayPositions > 1) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
    }

    /**
     * Проверяет добавление новой позиции дисплея к существующей комплектации.
     */
    private void validateSingleDisplay(
            Map<Equipment, Integer> first,
            Map<Equipment, Integer> second,
            Equipment candidate
    ) {
        long displayPositions = java.util.stream.Stream.concat(
                        first.keySet().stream(),
                        second.keySet().stream()
                )
                .filter(Equipment::isDisplay)
                .count();

        if (candidate isDisplay() && displayPositions >= 1) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
    }

    public static EquipmentConfiguration fromItems(List<EquipmentItem> required,
                                                   List<EquipmentItem> additional) {
        EquipmentConfiguration result = new EquipmentConfiguration(List.of(), List.of());
        if (required != null) {
            required.forEach(item -> result.addRequiredEquipment(item.equipment(), item.quantity()));
        }
        if (additional != null) {
            additional.forEach(item -> result.addAdditionalEquipment(item.equipment(), item.quantity()));
        }
        return result;
    }

    public EquipmentConfiguration copy() {
        return fromItems(getRequiredItems(), getAdditionalItems());
    }

    public List<Equipment> getRequiredEquipment() {
        return List.copyOf(requiredEquipment.keySet());
    }

    public List<Equipment> getAdditionalEquipment() {
        return List.copyOf(additionalEquipment.keySet());
    }

    /** Возвращает уникальные позиции без разворачивания количества. */
    public List<Equipment> getAllEquipment() {
        List<Equipment> result = new ArrayList<>(requiredEquipment.keySet());
        result.addAll(additionalEquipment.keySet());
        result.sort(Comparator.comparing(Equipment::getCatalogPositionId));
        return List.copyOf(result);
    }

    public List<EquipmentItem> getRequiredItems() { return toItems(requiredEquipment); }
    public List<EquipmentItem> getAdditionalItems() { return toItems(additionalEquipment); }

    public List<EquipmentItem> getAllItems() {
        List<EquipmentItem> result = new ArrayList<>(getRequiredItems());
        result.addAll(getAdditionalItems());
        result.sort(Comparator.comparing(
                item -> item.equipment().getCatalogPositionId()
        ));
        return List.copyOf(result);
    }

    private List<EquipmentItem> toItems(Map<Equipment, Integer> source) {
        return source.entrySet().stream()
                .map(entry -> new EquipmentItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    public int getQuantity(Equipment equipment) {
        Objects.requireNonNull(equipment, "Оборудование не должно быть null");
        return requiredEquipment.getOrDefault(
                equipment, additionalEquipment.getOrDefault(equipment, 0));
    }

    public void addRequiredEquipment(Equipment equipment) {
        addRequiredEquipment(equipment, 1);
    }

    public void addRequiredEquipment(Equipment equipment, int quantity) {
        addEquipment(requiredEquipment, additionalEquipment, equipment, quantity);
    }

    public void addAdditionalEquipment(Equipment equipment) {
        addAdditionalEquipment(equipment, 1);
    }

    public void addAdditionalEquipment(Equipment equipment, int quantity) {
        addEquipment(additionalEquipment, requiredEquipment, equipment, quantity);
    }

    private void addEquipment(Map<Equipment, Integer> target,
                              Map<Equipment, Integer> other,
                              Equipment equipment, int quantity) {
        Equipment position = Objects.requireNonNull(
                equipment, "Оборудование не должно быть null");
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Количество оборудования должно быть не меньше 1");
        }
        if (other.containsKey(position)) {
            throw new IllegalArgumentException(
                    "Позиция уже добавлена в другую группу оборудования: " +
                            position.getCatalogPositionId());
        }
        int currentQuantity = target.getOrDefault(position, 0);
        int newQuantity = sumQuantities(currentQuantity, quantity);

        if (position isDisplay() && newQuantity > 1) {
            throw new IllegalArgumentException(
                    "Количество дисплеев в модуле должно быть равно 1");
        }

        if (position isDisplay() && currentQuantity == 0) {
            validateSingleDisplay(target, other, position);
        }

        target.put(position, newQuantity);
    }

    /**
     * Складывает количества без переполнения целочисленного диапазона.
     *
     * @param current текущее количество
     * @param additional добавляемое количество
     * @return итоговое количество
     * @throws IllegalArgumentException если сумма превышает диапазон int
     */
    private static int sumQuantities(int current, int additional) {
        try {
            return Math.addExact(current, additional);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Количество оборудования превышает допустимый диапазон",
                    exception
            );
        }
    }

    public boolean removeRequiredEquipment(Equipment equipment) {
        return requiredEquipment.remove(
                Objects.requireNonNull(equipment, "Оборудование не должно быть null")) != null;
    }

    public boolean removeAdditionalEquipment(Equipment equipment) {
        return additionalEquipment.remove(
                Objects.requireNonNull(equipment, "Оборудование не должно быть null")) != null;
    }

    /**
     * Возвращает состав оборудования без учёта группы, в которой
     * позиция была сформирована. Группа нужна для правил изменения
     * конфигурации, но не меняет физическую комплектацию модуля.
     */
    private Map<Equipment, Integer> getEquipmentQuantities() {
        Map<Equipment, Integer> result =
                new LinkedHashMap<>(requiredEquipment);
        result.putAll(additionalEquipment);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentConfiguration other)) return false;
        return getEquipmentQuantities().equals(other.getEquipmentQuantities());
    }

    @Override
    public int hashCode() {
        return getEquipmentQuantities().hashCode();
    }

    @Override
    public String toString() {
        return "EquipmentConfiguration{" +
                "requiredEquipment=" + getRequiredItems() +
                ", additionalEquipment=" + getAdditionalItems() + '}';
    }
}