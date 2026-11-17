package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Конфигурация оборудования модуля управления.
 *
 * <p>Разделяет оборудование на две группы:</p>
 * <ul>
 *     <li>обязательное оборудование;</li>
 *     <li>дополнительное оборудование.</li>
 * </ul>
 *
 * <p>Класс не содержит настроек приложения и не знает о Spring.
 * Значения по умолчанию могут быть загружены конфигурационным
 * слоем приложения и переданы в конструктор.</p>
 */
public class EquipmentConfiguration {

    /**
     * Оборудование, обязательное для конфигурации модуля.
     */
    private final List<Equipment> requiredEquipment;

    /**
     * Дополнительное оборудование.
     */
    private final List<Equipment> additionalEquipment;

    /**
     * Создаёт конфигурацию оборудования.
     *
     * <p>Если один из списков не задан, вместо него используется
     * пустой список.</p>
     *
     * @param requiredEquipment обязательное оборудование
     * @param additionalEquipment дополнительное оборудование
     */
    public EquipmentConfiguration(
            List<Equipment> requiredEquipment,
            List<Equipment> additionalEquipment
    ) {
        this.requiredEquipment = copyEquipment(
                requiredEquipment,
                "Обязательное оборудование"
        );

        this.additionalEquipment = copyEquipment(
                additionalEquipment,
                "Дополнительное оборудование"
        );
    }

    /**
     * Создаёт независимый список оборудования и проверяет его элементы.
     *
     * @param source исходный список
     * @param listName название списка для сообщения об ошибке
     * @return независимый список оборудования
     */
    private List<Equipment> copyEquipment(
            List<Equipment> source,
            String listName
    ) {
        List<Equipment> result = new ArrayList<>();

        if (source == null) {
            return result;
        }

        for (Equipment equipment : source) {
            result.add(
                    Objects.requireNonNull(
                            equipment,
                            listName + " не должно содержать null"
                    )
            );
        }

        return result;
    }

    /**
     * Создаёт независимую копию конфигурации оборудования.
     *
     * <p>Объекты оборудования неизменяемы, поэтому копируются
     * списки, а экземпляры оборудования безопасно переиспользуются.</p>
     *
     * @return независимая копия конфигурации
     */
    public EquipmentConfiguration copy() {
        return new EquipmentConfiguration(
                requiredEquipment,
                additionalEquipment
        );
    }

    /**
     * Возвращает обязательное оборудование.
     *
     * <p>Возвращается представление списка только для чтения.</p>
     *
     * @return список обязательного оборудования
     */
    public List<Equipment> getRequiredEquipment() {
        return Collections.unmodifiableList(requiredEquipment);
    }

    /**
     * Возвращает дополнительное оборудование.
     *
     * <p>Возвращается представление списка только для чтения.</p>
     *
     * @return список дополнительного оборудования
     */
    public List<Equipment> getAdditionalEquipment() {
        return Collections.unmodifiableList(additionalEquipment);
    }

    /**
     * Возвращает всё оборудование конфигурации.
     *
     * <p>Сначала возвращается обязательное оборудование,
     * затем дополнительное.</p>
     *
     * @return неизменяемый список всего оборудования
     */
    public List<Equipment> getAllEquipment() {
        List<Equipment> equipment =
                new ArrayList<>(requiredEquipment);

        equipment.addAll(additionalEquipment);

        return Collections.unmodifiableList(equipment);
    }

    /**
     * Добавляет оборудование в список обязательного оборудования.
     *
     * @param equipment добавляемое оборудование
     * @throws NullPointerException если оборудование не задано
     */
    public void addRequiredEquipment(Equipment equipment) {
        requiredEquipment.add(
                Objects.requireNonNull(
                        equipment,
                        "Оборудование не должно быть null"
                )
        );
    }

    /**
     * Добавляет оборудование в список дополнительного оборудования.
     *
     * @param equipment добавляемое оборудование
     * @throws NullPointerException если оборудование не задано
     */
    public void addAdditionalEquipment(Equipment equipment) {
        additionalEquipment.add(
                Objects.requireNonNull(
                        equipment,
                        "Оборудование не должно быть null"
                )
        );
    }

    /**
     * Удаляет оборудование из списка обязательного оборудования.
     *
     * <p>Если оборудование отсутствует, список не изменяется.</p>
     *
     * @param equipment удаляемое оборудование
     * @return {@code true}, если оборудование было удалено
     */
    public boolean removeRequiredEquipment(Equipment equipment) {
        return requiredEquipment.remove(
                Objects.requireNonNull(
                        equipment,
                        "Оборудование не должно быть null"
                )
        );
    }

    /**
     * Удаляет оборудование из списка дополнительного оборудования.
     *
     * <p>Если оборудование отсутствует, список не изменяется.</p>
     *
     * @param equipment удаляемое оборудование
     * @return {@code true}, если оборудование было удалено
     */
    public boolean removeAdditionalEquipment(Equipment equipment) {
        return additionalEquipment.remove(
                Objects.requireNonNull(
                        equipment,
                        "Оборудование не должно быть null"
                )
        );
    }

    /**
     * Сравнивает конфигурации оборудования.
     *
     * <p>Списки сравниваются с учётом их порядка.</p>
     *
     * @param o объект для сравнения
     * @return {@code true}, если конфигурации оборудования совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EquipmentConfiguration other)) {
            return false;
        }

        return Objects.equals(
                requiredEquipment,
                other.requiredEquipment
        ) && Objects.equals(
                additionalEquipment,
                other.additionalEquipment
        );
    }

    /**
     * Возвращает хэш-код конфигурации оборудования.
     *
     * @return хэш-код конфигурации
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                requiredEquipment,
                additionalEquipment
        );
    }

    /**
     * Возвращает подробное строковое представление конфигурации.
     *
     * @return строковое представление конфигурации оборудования
     */
    @Override
    public String toString() {
        return "EquipmentConfiguration{" +
                "requiredEquipment=" + requiredEquipment +
                ", additionalEquipment=" + additionalEquipment +
                '}';
    }
}