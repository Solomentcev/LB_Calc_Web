package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.entity.LCEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Маппер между LCEntity и доменной моделью LC.
 *
 * <p>Отвечает только за преобразование persistence-модели
 * в domain-модель и обратно.</p>
 */
public final class LCEntityMapper {

    private LCEntityMapper() {
    }

    /**
     * Entity -> Domain.
     *
     * <p>Производные поля name и description из Entity
     * не переносятся. Domain формирует их самостоятельно.</p>
     */
    public static LC toDomain(LCEntity entity) {
        if (entity == null) {
            return null;
        }

        Payment payment = Objects.requireNonNull(
                entity.getPayment(),
                "Способ оплаты LC не должен быть null"
        );

        EquipmentConfiguration equipmentConfiguration =
                toEquipmentConfiguration(entity);

        ControlConfiguration controlConfiguration =
                new ControlConfiguration(
                        payment,
                        entity.getAccessMethods(),
                        entity.getPrintOptions(),
                        equipmentConfiguration
                );

        return new LC(
                entity.getHeight(),
                entity.getWidth(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                Objects.requireNonNull(
                        entity.getColorBody(),
                        "Цвет корпуса LC не должен быть null"
                ),
                Objects.requireNonNull(
                        entity.getColorDoor(),
                        "Цвет дверей LC не должен быть null"
                ),
                controlConfiguration
        );
    }

    /**
     * Domain -> новая Entity.
     */
    public static LCEntity toEntity(LC domain) {
        if (domain == null) {
            return null;
        }

        LCEntity entity = new LCEntity();

        updateEntity(domain, entity);

        return entity;
    }

    /**
     * Обновляет существующую Entity данными Domain.
     *
     * <p>Идентификатор Entity не изменяется.</p>
     */
    public static void updateEntity(
            LC domain,
            LCEntity entity
    ) {
        if (domain == null) {
            throw new IllegalArgumentException(
                    "LC не должен быть null"
            );
        }

        if (entity == null) {
            throw new IllegalArgumentException(
                    "LCEntity не должен быть null"
            );
        }

        /*
         * Общие параметры ModuleEntity.
         */
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        entity.setHeight(domain.getHeight());
        entity.setWidth(domain.getWidth());
        entity.setDepth(domain.getDepth());

        entity.setUpperFrame(domain.getUpperFrame());
        entity.setBottomFrame(domain.getBottomFrame());

        entity.setColorBody(domain.getColorBody());
        entity.setColorDoor(domain.getColorDoor());

        /*
         * Конфигурация управления.
         */
        ControlConfiguration configuration =
                domain.getControlConfiguration();

        entity.setPayment(configuration.getPayment());

        entity.setAccessMethods(
                configuration.getAccessMethods()
        );

        entity.setPrintOptions(
                configuration.getPrintOptions()
        );

        /*
         * Оборудование.
         */
        updateEquipment(configuration, entity);
    }

    /**
     * Восстанавливает EquipmentConfiguration из Entity.
     */
    private static EquipmentConfiguration toEquipmentConfiguration(
            LCEntity entity
    ) {
        List<Equipment> required = new ArrayList<>();

        addDisplay(entity.getDisplay(), required);
        addBarReader(entity.getBarReader(), required);

        if (entity.isPrinter()) {
            required.add(new Printer());
        }

        if (entity.isRfidReader()) {
            required.add(new RfidReader());
        }

        /*
         * Entity не хранит разделение на required/additional.
         * Поэтому всё сохранённое оборудование восстанавливаем
         * как часть текущей конфигурации.
         */
        return new EquipmentConfiguration(
                required,
                List.of()
        );
    }

    /**
     * Записывает конфигурацию оборудования в Entity.
     */
    private static void updateEquipment(
            ControlConfiguration configuration,
            LCEntity entity
    ) {
        List<Equipment> equipment =
                configuration.getEquipmentConfiguration()
                        .getAllEquipment();

        Display display = equipment.stream()
                .filter(Display.class::isInstance)
                .map(Display.class::cast)
                .filter(value -> value != Display.NONE)
                .findFirst()
                .orElse(null);

        BarReader barReader = equipment.stream()
                .filter(BarReader.class::isInstance)
                .map(BarReader.class::cast)
                .filter(value -> value != BarReader.NONE)
                .findFirst()
                .orElse(null);

        boolean printer = equipment.stream()
                .anyMatch(Printer.class::isInstance);

        boolean rfidReader = equipment.stream()
                .anyMatch(RfidReader.class::isInstance);

        entity.setDisplay(
                display != null
                        ? display.getName()
                        : null
        );

        entity.setBarReader(
                barReader != null
                        ? barReader.getName()
                        : null
        );

        entity.setPrinter(printer);
        entity.setRfidReader(rfidReader);
    }

    private static void addDisplay(
            String name,
            List<Equipment> equipment
    ) {
        if (name == null || name.isBlank()) {
            return;
        }

        if (name.equals(Display.NONE.getName())) {
            return;
        }

        for (Display display : List.of(
                Display.LC10,
                Display.LC17,
                Display.LC19
        )) {
            if (display.getName().equals(name)) {
                equipment.add(display);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Неизвестный дисплей LC: " + name
        );
    }

    private static void addBarReader(
            String name,
            List<Equipment> equipment
    ) {
        if (name == null || name.isBlank()) {
            return;
        }

        if (name.equals(BarReader.NONE.getName())) {
            return;
        }

        for (BarReader reader : List.of(
                BarReader.READER_1D,
                BarReader.READER_2D
        )) {
            if (reader.getName().equals(name)) {
                equipment.add(reader);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Неизвестный сканер штрихкода LC: " + name
        );
    }
}