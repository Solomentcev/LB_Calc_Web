package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.entity.LBCEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Маппер между LBCEntity и доменной моделью LBC.
 */
public final class LBCEntityMapper {

    private LBCEntityMapper() {
    }

    /**
     * Entity -> Domain.
     */
    public static LBC toDomain(LBCEntity entity) {
        if (entity == null) {
            return null;
        }

        TypeLb typeLb = new TypeLb(
                entity.getType(),
                entity.getDeltaWidth(),
                entity.getShelfThick(),
                entity.getServiceZoneWidth()
        );

        ControlConfiguration controlConfiguration =
                new ControlConfiguration(
                        Objects.requireNonNull(
                                entity.getPayment(),
                                "Способ оплаты LBC не должен быть null"
                        ),
                        entity.getAccessMethods(),
                        entity.getPrintOptions(),
                        toEquipmentConfiguration(entity)
                );

        return new LBC(
                entity.getHeight(),
                entity.getWidth(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                Objects.requireNonNull(
                        entity.getColorBody(),
                        "Цвет корпуса LBC не должен быть null"
                ),
                Objects.requireNonNull(
                        entity.getColorDoor(),
                        "Цвет дверей LBC не должен быть null"
                ),
                typeLb,
                entity.getCountCells(),
                Objects.requireNonNull(
                        entity.getDirectionDoorOpening(),
                        "Направление открытия дверей LBC не должно быть null"
                ),
                entity.getDoorThickness(),
                controlConfiguration
        );
    }

    /**
     * Domain -> новая Entity.
     */
    public static LBCEntity toEntity(LBC domain) {
        if (domain == null) {
            return null;
        }

        LBCEntity entity = new LBCEntity();

        updateEntity(domain, entity);

        return entity;
    }

    /**
     * Обновляет существующую Entity данными Domain.
     */
    public static void updateEntity(
            LBC domain,
            LBCEntity entity
    ) {
        if (domain == null) {
            throw new IllegalArgumentException(
                    "LBC не должен быть null"
            );
        }

        if (entity == null) {
            throw new IllegalArgumentException(
                    "LBCEntity не должен быть null"
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
         * TypeLb.
         */
        TypeLb typeLb = domain.getTypeLb();

        entity.setType(typeLb.getType());
        entity.setDeltaWidth(typeLb.getDeltaWidth());
        entity.setShelfThick(typeLb.getShelfThick());
        entity.setServiceZoneWidth(typeLb.getServiceZoneWidth());

        /*
         * Storage-параметры.
         */
        entity.setDoorThickness(domain.getDoorThickness());
        entity.setDirectionDoorOpening(
                domain.getDirectionDoorOpening()
        );
        entity.setCountCells(domain.getCountCells());

        entity.setHeightCell(domain.getCellHeight());
        entity.setWidthCell(domain.getCellWidth());
        entity.setDepthCell(domain.getCellDepth());

        /*
         * ControlConfiguration.
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

        updateEquipment(configuration, entity);
    }

    /**
     * Восстанавливает конфигурацию оборудования из Entity.
     */
    private static EquipmentConfiguration toEquipmentConfiguration(
            LBCEntity entity
    ) {
        List<Equipment> equipment = new ArrayList<>();

        addDisplay(entity.getDisplay(), equipment);
        addBarReader(entity.getBarReader(), equipment);

        if (entity.isPrinter()) {
            equipment.add(new Printer());
        }

        if (entity.isRfidReader()) {
            equipment.add(new RfidReader());
        }

        return new EquipmentConfiguration(
                equipment,
                List.of()
        );
    }

    /**
     * Сохраняет конфигурацию оборудования в Entity.
     */
    private static void updateEquipment(
            ControlConfiguration configuration,
            LBCEntity entity
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

        entity.setPrinter(
                equipment.stream()
                        .anyMatch(Printer.class::isInstance)
        );

        entity.setRfidReader(
                equipment.stream()
                        .anyMatch(RfidReader.class::isInstance)
        );
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
                "Неизвестный дисплей LBC: " + name
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
                "Неизвестный сканер штрихкода LBC: " + name
        );
    }
}

