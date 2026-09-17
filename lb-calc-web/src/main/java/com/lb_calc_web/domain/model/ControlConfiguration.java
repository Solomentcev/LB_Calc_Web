package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Конфигурация функций управления модулем.
 *
 * <p>Определяет способ оплаты, доступные способы доступа,
 * варианты печати и состав оборудования, используемого
 * модулем управления.</p>
 *
 * <p>Порядок {@link AccessMethod} и {@link PrintOption} не имеет
 * значения, поэтому они хранятся как множества.</p>
 *
 * <p>Значения по умолчанию не зашиваются в domain-класс.
 * Конфигурационный слой приложения может получить их из
 * {@code size-bounds.properties} и передать готовую конфигурацию
 * при создании объекта.</p>
 */
public class ControlConfiguration {

    /**
     * Способ оплаты.
     */
    private final Payment payment;

    /**
     * Допустимые способы доступа.
     */
    private final Set<AccessMethod> accessMethods;

    /**
     * Доступные варианты печати.
     */
    private final Set<PrintOption> printOptions;

    /**
     * Конфигурация оборудования.
     */
    private final EquipmentConfiguration equipmentConfiguration;

    /**
     * Создаёт конфигурацию управления.
     *
     * <p>Если список способов доступа или печати не задан,
     * используется пустое множество.</p>
     *
     * @param payment способ оплаты
     * @param accessMethods допустимые способы доступа
     * @param printOptions варианты печати
     * @param equipmentConfiguration конфигурация оборудования
     * @throws NullPointerException если {@code payment} или
     *                              {@code equipmentConfiguration} равны null
     */
    public ControlConfiguration(
            Payment payment,
            Set<AccessMethod> accessMethods,
            Set<PrintOption> printOptions,
            EquipmentConfiguration equipmentConfiguration
    ) {
        this.payment = Objects.requireNonNull(
                payment,
                "Способ оплаты не должен быть null"
        );

        this.accessMethods = copyAccessMethods(accessMethods);
        this.printOptions = copyPrintOptions(printOptions);

        this.equipmentConfiguration = Objects.requireNonNull(
                equipmentConfiguration,
                "Конфигурация оборудования не должна быть null"
        );
    }

    /**
     * Копирует набор способов доступа в независимый {@link EnumSet}.
     *
     * @param accessMethods исходный набор
     * @return копия набора или пустой набор
     */
    private Set<AccessMethod> copyAccessMethods(
            Set<AccessMethod> accessMethods
    ) {
        EnumSet<AccessMethod> result =
                EnumSet.noneOf(AccessMethod.class);

        if (accessMethods != null) {
            result.addAll(accessMethods);
        }

        return result;
    }

    /**
     * Копирует набор вариантов печати в независимый {@link EnumSet}.
     *
     * @param printOptions исходный набор
     * @return копия набора или пустой набор
     */
    private Set<PrintOption> copyPrintOptions(
            Set<PrintOption> printOptions
    ) {
        EnumSet<PrintOption> result =
                EnumSet.noneOf(PrintOption.class);

        if (printOptions != null) {
            result.addAll(printOptions);
        }

        return result;
    }

    /**
     * Возвращает способ оплаты.
     *
     * @return способ оплаты
     */
    public Payment getPayment() {
        return payment;
    }

    /**
     * Возвращает доступные способы доступа.
     *
     * <p>Возвращается представление только для чтения.</p>
     *
     * @return набор способов доступа
     */
    public Set<AccessMethod> getAccessMethods() {
        return Collections.unmodifiableSet(accessMethods);
    }

    /**
     * Возвращает варианты печати.
     *
     * <p>Возвращается представление только для чтения.</p>
     *
     * @return набор вариантов печати
     */
    public Set<PrintOption> getPrintOptions() {
        return Collections.unmodifiableSet(printOptions);
    }

    /**
     * Возвращает конфигурацию оборудования.
     *
     * @return конфигурация оборудования
     */
    public EquipmentConfiguration getEquipmentConfiguration() {
        return equipmentConfiguration;
    }

    /**
     * Сравнивает конфигурации управления по всем параметрам.
     *
     * @param o объект для сравнения
     * @return {@code true}, если конфигурации совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ControlConfiguration other)) {
            return false;
        }

        return payment == other.payment
                && Objects.equals(
                accessMethods,
                other.accessMethods
        )
                && Objects.equals(
                printOptions,
                other.printOptions
        )
                && Objects.equals(
                equipmentConfiguration,
                other.equipmentConfiguration
        );
    }

    /**
     * Возвращает хэш-код конфигурации управления.
     *
     * @return хэш-код конфигурации
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                payment,
                accessMethods,
                printOptions,
                equipmentConfiguration
        );
    }

    /**
     * Возвращает подробное строковое представление конфигурации.
     *
     * @return строковое представление конфигурации управления
     */
    @Override
    public String toString() {
        return "ControlConfiguration{" +
                "payment=" + payment +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                ", equipmentConfiguration=" +
                equipmentConfiguration +
                '}';
    }
}