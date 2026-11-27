package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.EquipmentPurpose;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Конфигурация управляющего модуля: функциональные возможности и установленное оборудование. */
public class ControlConfiguration {

    private static final FunctionalConfigurationResolver RESOLVER =
            new FunctionalConfigurationResolver();

    private FunctionalConfiguration functionalConfiguration;
    private final Map<Equipment, Integer> equipment;

    public ControlConfiguration(FunctionalConfiguration functionalConfiguration,
                                Map<Equipment, Integer> equipment) {
        this.functionalConfiguration = RESOLVER.normalize(Objects.requireNonNull(
                functionalConfiguration, "Функциональная конфигурация не должна быть null"));
        Map<Equipment, Integer> selected = copyEquipment(equipment);
        validateEquipment(selected);
        this.equipment = new LinkedHashMap<>(
                RESOLVER.retainRequiredEquipment(this.functionalConfiguration, selected));
    }

    /** Создаёт независимую карту оборудования и проверяет количества моделей. */
    private static Map<Equipment, Integer> copyEquipment(Map<Equipment, Integer> source) {
        Objects.requireNonNull(source, "Состав оборудования не должен быть null");
        Map<Equipment, Integer> result = new LinkedHashMap<>();
        source.forEach((item, quantity) -> {
            Equipment model = Objects.requireNonNull(item, "Оборудование не должно быть null");
            Integer count = Objects.requireNonNull(quantity, "Количество оборудования не должно быть null");
            if (count < 1) throw new IllegalArgumentException(
                    "Количество оборудования должно быть не меньше 1");
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

    /** Проверяет ограничения состава установленного оборудования. */
    private static void validateEquipment(Map<Equipment, Integer> equipment) {
        long displayCount = equipment.entrySet().stream()
                .filter(entry -> entry.getKey().getCategory() == EquipmentCategory.DISPLAY)
                .mapToLong(Map.Entry::getValue).sum();
        if (displayCount > 1) {
            throw new IllegalArgumentException(
                    "В модуле управления может быть установлен только один дисплей");
        }
    }

    /** @return копия функциональных назначений, исключающая внешнюю модификацию состояния */
    public FunctionalConfiguration getFunctionalConfiguration() {
        return functionalConfiguration.copy();
    }

    /**
     * Обновляет функциональные назначения и удаляет оборудование, которое больше им не соответствует.
     * Недостающие модели подбираются методом {@link #configureEquipment} из каталога.
     */
    public void setFunctionalConfiguration(FunctionalConfiguration configuration) {
        FunctionalConfiguration normalized = RESOLVER.normalize(Objects.requireNonNull(
                configuration, "Функциональная конфигурация не должна быть null"));
        Map<Equipment, Integer> compatible =
                RESOLVER.retainRequiredEquipment(normalized, equipment);
        equipment.clear();
        equipment.putAll(compatible);
        functionalConfiguration = normalized;
    }


    /**
     * Согласует функциональные назначения с каталогом и подбирает физические модели.
     * Уже выбранные совместимые модели и их количества сохраняются; системное оборудование
     * не затрагивается функциональными требованиями.
     */
    public void configureEquipment(
            FunctionalConfiguration configuration,
            java.util.Collection<Equipment> catalog,
            java.util.Set<Equipment> defaultModels
    ) {
        Objects.requireNonNull(catalog, "Каталог оборудования не должен быть null");
        Objects.requireNonNull(defaultModels, "Модели по умолчанию не должны быть null");

        FunctionalConfiguration normalized = RESOLVER.normalize(Objects.requireNonNull(
                configuration, "Функциональная конфигурация не должна быть null"));
        Map<Equipment, Integer> selected =
                new LinkedHashMap<>(RESOLVER.retainRequiredEquipment(normalized, equipment));
        Map<EquipmentFunctionalType, java.util.Set<EquipmentPurpose>> requirements =
                RESOLVER.resolveRequiredEquipment(normalized);

        boolean hasSeparatePayment = selected.keySet().stream().anyMatch(model ->
                model.getFunctionalType() == EquipmentFunctionalType.PAYMENT
                        && (model.getPurpose() == com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS
                        || model.getPurpose() == com.lb_calc_web.domain.equipment.PaymentPurpose.CASH));
        boolean needsCardsAndCash = requirements.getOrDefault(
                EquipmentFunctionalType.PAYMENT, java.util.Set.of()).contains(
                com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS)
                && requirements.getOrDefault(EquipmentFunctionalType.PAYMENT, java.util.Set.of()).contains(
                com.lb_calc_web.domain.equipment.PaymentPurpose.CASH);

        if (needsCardsAndCash && !hasSeparatePayment
                && selected.keySet().stream().noneMatch(model ->
                model.getFunctionalType() == EquipmentFunctionalType.PAYMENT
                        && model.getPurpose() == com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS_AND_CASH)) {
            Equipment combined = selectModel(catalog, defaultModels,
                    EquipmentFunctionalType.PAYMENT,
                    com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS_AND_CASH);
            if (combined != null) {
                selected.put(combined, 1);
            }
        }

        requirements.forEach((type, purposes) -> purposes.forEach(purpose -> {
            boolean covered = selected.keySet().stream().anyMatch(model ->
                    model.getFunctionalType() == type && RESOLVER.supportsRequirement(model, purpose));
            if (!covered) {
                Equipment model = selectModel(catalog, defaultModels, type, purpose);
                if (model != null) {
                    selected.put(model, 1);
                }
            }
        }));

        equipment.clear();
        equipment.putAll(selected);
        functionalConfiguration = normalized;
    }

    /** Выбирает модель по умолчанию, а при единственном варианте — автоматически. */
    private Equipment selectModel(
            java.util.Collection<Equipment> catalog,
            java.util.Set<Equipment> defaultModels,
            EquipmentFunctionalType type,
            EquipmentPurpose purpose
    ) {
        java.util.List<Equipment> candidates = catalog.stream()
                .filter(model -> model.getFunctionalType() == type && model.getPurpose().equals(purpose))
                .toList();
        Equipment preferred = candidates.stream().filter(defaultModels::contains).findFirst().orElse(null);
        if (preferred != null) {
            return preferred;
        }
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    /** Возвращает требования к физической комплектации для текущих функциональных назначений. */
    public Map<EquipmentFunctionalType, java.util.Set<EquipmentPurpose>> getRequiredEquipment() {
        return RESOLVER.resolveRequiredEquipment(functionalConfiguration);
    }

    /**
     * Проверяет, что выбранная физическая комплектация покрывает все функциональные требования
     * и не содержит лишнего оборудования.
     */
    public void validateEquipmentRequirements() {
        Map<EquipmentFunctionalType, java.util.Set<EquipmentPurpose>> missing =
                RESOLVER.findMissingRequirements(functionalConfiguration, equipment);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Не выбрано физическое оборудование для функциональных назначений: " + missing);
        }
        Map<Equipment, Integer> compatible =
                RESOLVER.retainRequiredEquipment(functionalConfiguration, equipment);
        if (compatible.size() != equipment.size()) {
            throw new IllegalStateException(
                    "Выбранное физическое оборудование не соответствует функциональной конфигурации");
        }
        getRequiredEquipment().forEach((type, purposes) -> purposes.forEach(purpose -> {
            long modelCount = equipment.keySet().stream()
                    .filter(model -> model.getFunctionalType() == type)
                    .filter(model -> RESOLVER.supportsRequirement(model, purpose))
                    .count();
            if (modelCount > 1) {
                throw new IllegalStateException(
                        "Для назначения " + type + ":" + purpose
                                + " выбрано несколько моделей оборудования");
            }
        }));
    }

    /** @return неизменяемое соответствие моделей оборудования и их количества */
    public Map<Equipment, Integer> getEquipment() {
        return Map.copyOf(equipment);
    }

    /** @return установленное количество модели или {@code 0}, если она отсутствует */
    public int getQuantity(Equipment item) {
        return equipment.getOrDefault(
                Objects.requireNonNull(item, "Оборудование не должно быть null"), 0);
    }

    /** Добавляет оборудование и пересчитывает связанные функциональные назначения. */
    public void addEquipment(Equipment item, int quantity) {
        Equipment model = Objects.requireNonNull(item, "Оборудование не должно быть null");
        if (quantity < 1) throw new IllegalArgumentException(
                "Количество оборудования должно быть не меньше 1");
        Map<Equipment, Integer> updated = new LinkedHashMap<>(equipment);
        try {
            updated.merge(model, quantity, Math::addExact);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Количество оборудования превышает допустимый диапазон", exception);
        }
        replaceEquipment(updated);
    }

    /** Удаляет модель и пересчитывает связанные назначения. */
    public boolean removeEquipment(Equipment item) {
        Equipment model = Objects.requireNonNull(item, "Оборудование не должно быть null");
        if (!equipment.containsKey(model)) return false;
        Map<Equipment, Integer> updated = new LinkedHashMap<>(equipment);
        updated.remove(model);
        replaceEquipment(updated);
        return true;
    }

    /** Заменяет состав оборудования с пересчётом только затронутых функциональных областей. */
    public void setEquipment(Map<Equipment, Integer> equipment) {
        replaceEquipment(copyEquipment(equipment));
    }

    /** Заменяет состав оборудования и синхронизирует затронутые функциональные назначения. */
    private void replaceEquipment(Map<Equipment, Integer> updated) {
        validateEquipment(updated);
        FunctionalConfiguration resolved = RESOLVER.resolve(
                functionalConfiguration, equipment, updated);
        equipment.clear();
        equipment.putAll(updated);
        functionalConfiguration = RESOLVER.normalize(resolved);
    }

    /** @return независимая копия функциональных назначений и установленного оборудования */
    public ControlConfiguration copy() {
        return new ControlConfiguration(functionalConfiguration, equipment);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ControlConfiguration other)) return false;
        return functionalConfiguration.equals(other.functionalConfiguration)
                && equipment.equals(other.equipment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionalConfiguration, equipment);
    }

    @Override
    public String toString() {
        return "ControlConfiguration{" +
                "functionalConfiguration=" + functionalConfiguration +
                ", equipment=" + equipment +
                '}';
    }
}
