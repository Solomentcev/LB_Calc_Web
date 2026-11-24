package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.List;
import java.util.Objects;

/**
 * Конфигурация оборудования модуля управления.
 *
 * <p>Функциональные возможности модуля определяются назначениями
 * установленного оборудования. Отдельные списки способов доступа,
 * оплаты и печати не хранятся, поскольку дублируют данные оборудования.</p>
 */
public class ControlConfiguration {

    private final EquipmentConfiguration equipmentConfiguration;

    /**
     * Создаёт конфигурацию управления.
     *
     * @param equipmentConfiguration состав оборудования
     */
    public ControlConfiguration(EquipmentConfiguration equipmentConfiguration) {
        this.equipmentConfiguration = Objects.requireNonNull(
                equipmentConfiguration,
                "Конфигурация оборудования не должна быть null"
        ).copy();
    }

    /**
     * Создаёт независимую копию конфигурации.
     *
     * @return копия конфигурации
     */
    public ControlConfiguration copy() {
        return new ControlConfiguration(equipmentConfiguration);
    }

    /**
     * Возвращает независимую копию конфигурации оборудования.
     *
     * @return конфигурация оборудования
     */
    public EquipmentConfiguration getEquipmentConfiguration() {
        return equipmentConfiguration.copy();
    }

    /**
     * Возвращает все позиции оборудования.
     *
     * @return неизменяемый список оборудования
     */
    public List<Equipment> getAllEquipment() {
        return equipmentConfiguration.getAllEquipment();
    }

    /**
     * Добавляет оборудование в обязательный состав.
     *
     * @param equipment добавляемое оборудование
     */
    public void addRequiredEquipment(Equipment equipment) {
        equipmentConfiguration.addRequiredEquipment(equipment);
    }

    /**
     * Добавляет оборудование в дополнительный состав.
     *
     * @param equipment добавляемое оборудование
     */
    public void addAdditionalEquipment(Equipment equipment) {
        equipmentConfiguration.addAdditionalEquipment(equipment);
    }

    /**
     * Удаляет оборудование из конфигурации.
     *
     * @param equipment удаляемое оборудование
     * @return true, если позиция была удалена
     */
    public boolean removeEquipment(Equipment equipment) {
        Objects.requireNonNull(
                equipment,
                "Оборудование не должно быть null"
        );

        boolean removedRequired =
                equipmentConfiguration.removeRequiredEquipment(equipment);
        boolean removedAdditional =
                equipmentConfiguration.removeAdditionalEquipment(equipment);

        return removedRequired || removedAdditional;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ControlConfiguration other)) {
            return false;
        }
        return equipmentConfiguration.equals(other.equipmentConfiguration);
    }

    @Override
    public int hashCode() {
        return equipmentConfiguration.hashCode();
    }

    @Override
    public String toString() {
        return "ControlConfiguration{" +
                "equipmentConfiguration=" + equipmentConfiguration +
                '}';
    }
}
