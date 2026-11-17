package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.EquipmentQuantityEmbeddable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Преобразует LBC между доменной и persistence-моделями. */
public final class LBCEntityMapper {
    private LBCEntityMapper() { }

    public static LBC toDomain(LBCEntity entity) {
        if (entity == null) return null;
        TypeLb typeLb = new TypeLb(entity.getType(), entity.getDeltaWidth(),
                entity.getShelfThick(), entity.getServiceZoneWidth());
        ControlConfiguration configuration = new ControlConfiguration(
                Objects.requireNonNull(entity.getPayment(),
                        "Способ оплаты LBC не должен быть null"),
                entity.getAccessMethods(), entity.getPrintOptions(),
                toEquipmentConfiguration(entity));
        return new LBC(
                entity.getHeight(), entity.getWidth(), entity.getDepth(),
                entity.getUpperFrame(), entity.getBottomFrame(),
                Objects.requireNonNull(entity.getColorBody(),
                        "Цвет корпуса LBC не должен быть null"),
                Objects.requireNonNull(entity.getColorDoor(),
                        "Цвет дверей LBC не должен быть null"),
                typeLb, entity.getCountCells(),
                Objects.requireNonNull(entity.getDirectionDoorOpening(),
                        "Направление открытия дверей LBC не должно быть null"),
                entity.getDoorThickness(), configuration);
    }

    public static LBCEntity toEntity(LBC domain) {
        if (domain == null) return null;
        LBCEntity entity = new LBCEntity();
        updateEntity(domain, entity);
        return entity;
    }

    public static void updateEntity(LBC domain, LBCEntity entity) {
        if (domain == null || entity == null) {
            throw new IllegalArgumentException("LBC и LBCEntity не должны быть null");
        }
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setHeight(domain.getHeight());
        entity.setWidth(domain.getWidth());
        entity.setDepth(domain.getDepth());
        entity.setUpperFrame(domain.getUpperFrame());
        entity.setBottomFrame(domain.getBottomFrame());
        entity.setColorBody(domain.getColorBody());
        entity.setColorDoor(domain.getColorDoor());

        TypeLb typeLb = domain.getTypeLb();
        entity.setType(typeLb.getType());
        entity.setDeltaWidth(typeLb.getDeltaWidth());
        entity.setShelfThick(typeLb.getShelfThick());
        entity.setServiceZoneWidth(typeLb.getServiceZoneWidth());

        entity.setDoorThickness(domain.getDoorThickness());
        entity.setDirectionDoorOpening(domain.getDirectionDoorOpening());
        entity.setCountCells(domain.getCountCells());
        entity.setHeightCell(domain.getCellHeight());
        entity.setWidthCell(domain.getCellWidth());
        entity.setDepthCell(domain.getCellDepth());

        ControlConfiguration configuration = domain.getControlConfiguration();
        entity.setPayment(configuration.getPayment());
        entity.setAccessMethods(configuration.getAccessMethods());
        entity.setPrintOptions(configuration.getPrintOptions());
        updateEquipment(configuration.getEquipmentConfiguration(), entity);
    }

    private static EquipmentConfiguration toEquipmentConfiguration(LBCEntity entity) {
        EquipmentConfiguration result = new EquipmentConfiguration(List.of(), List.of());
        addPersistedEquipment(entity.getDisplay(), entity.getDisplayQuantity(), result);
        if (entity.getBarReaders().isEmpty()) {
            // Обратная совместимость с записями старого формата.
            addPersistedEquipment(entity.getBarReader(), entity.getBarReaderQuantity(), result);
        } else {
            entity.getBarReaders().forEach(item ->
                    addPersistedEquipment(item.getCatalogPositionId(), item.getQuantity(), result));
        }
        addPersistedEquipment(Printer.CATALOG_POSITION_ID,
                entity.getPrinterQuantity() > 0 ? entity.getPrinterQuantity()
                        : entity.isPrinter() ? 1 : 0, result);
        addPersistedEquipment(RfidReader.CATALOG_POSITION_ID,
                entity.getRfidReaderQuantity() > 0 ? entity.getRfidReaderQuantity()
                        : entity.isRfidReader() ? 1 : 0, result);
        return result;
    }

    private static void addPersistedEquipment(String idOrName, int quantity,
                                               EquipmentConfiguration target) {
        if (idOrName == null || idOrName.isBlank() || quantity == 0) return;
        Equipment equipment = EquipmentCatalog.resolve(idOrName);
        if (equipment == Display.NONE || equipment == BarReader.NONE) return;
        target.addRequiredEquipment(equipment, quantity);
    }

    private static void updateEquipment(EquipmentConfiguration configuration,
                                        LBCEntity entity) {
        Equipment display = findEquipment(configuration, Display.class);
        Equipment reader = findEquipment(configuration, BarReader.class);
        int printerQuantity = quantity(configuration, Printer.CATALOG_POSITION_ID);
        int rfidQuantity = quantity(configuration, RfidReader.CATALOG_POSITION_ID);

        entity.setDisplay(display == null ? null : display.getCatalogPositionId());
        entity.setDisplayQuantity(display == null ? 0 : configuration.getQuantity(display));
        entity.setBarReader(reader == null ? null : reader.getCatalogPositionId());
        entity.setBarReaderQuantity(reader == null ? 0 : configuration.getQuantity(reader));
        entity.setBarReaders(configuration.getAllEquipment().stream()
                .filter(BarReader.class::isInstance)
                .map(BarReader.class::cast)
                .filter(item -> item != BarReader.NONE)
                .map(item -> new EquipmentQuantityEmbeddable(
                        item.getCatalogPositionId(), configuration.getQuantity(item)))
                .collect(Collectors.toSet()));
        entity.setPrinterQuantity(printerQuantity);
        entity.setRfidReaderQuantity(rfidQuantity);
        entity.setPrinter(printerQuantity > 0);
        entity.setRfidReader(rfidQuantity > 0);
    }

    private static Equipment findEquipment(EquipmentConfiguration configuration,
                                           Class<? extends Equipment> type) {
        return configuration.getAllEquipment().stream()
                .filter(type::isInstance)
                .filter(item -> item != Display.NONE && item != BarReader.NONE)
                .findFirst().orElse(null);
    }

    private static int quantity(EquipmentConfiguration configuration, String id) {
        return configuration.getAllEquipment().stream()
                .filter(item -> item.getCatalogPositionId().equals(id))
                .findFirst().map(configuration::getQuantity).orElse(0);
    }
}