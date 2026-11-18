package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
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
        LBC domain = new LBC(
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

        if (!domain.getName().equals(entity.getName())
                || !domain.getDescription().equals(entity.getDescription())
                || domain.getCellHeight() != entity.getHeightCell()
                || domain.getCellWidth() != entity.getWidthCell()
                || domain.getCellDepth() != entity.getDepthCell()) {
            throw new IllegalStateException(
                    "Производные характеристики LBC в БД не соответствуют исходным параметрам"
            );
        }

        return domain;
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
        entity.setHeightCell(domain.getCellHeight());
        entity.setWidthCell(domain.getCellWidth());
        entity.setDepthCell(domain.getCellDepth());
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

        ControlConfiguration configuration = domain.getControlConfiguration();
        entity.setPayment(configuration.getPayment());
        entity.setAccessMethods(configuration.getAccessMethods());
        entity.setPrintOptions(configuration.getPrintOptions());
        updateEquipment(configuration.getEquipmentConfiguration(), entity);
    }

    private static EquipmentConfiguration toEquipmentConfiguration(LBCEntity entity) {
        EquipmentConfiguration result = new EquipmentConfiguration(List.of(), List.of());
        entity.getEquipment().forEach(item ->
                addPersistedEquipment(
                        item.getCatalogPositionId(),
                        item.getQuantity(),
                        result
                )
        );
        return result;
    }

    private static void addPersistedEquipment(
            String catalogPositionId,
            int quantity,
            EquipmentConfiguration target
    ) {
        Equipment equipment = EquipmentCatalog.findByCatalogPositionId(catalogPositionId);
        if (equipment == Display.NONE || equipment == BarReader.NONE) {
            return;
        }
        target.addRequiredEquipment(equipment, quantity);
    }

    private static void updateEquipment(
            EquipmentConfiguration configuration,
            LBCEntity entity
    ) {
        entity.setEquipment(configuration.getAllEquipment().stream()
                .filter(item -> item != Display.NONE && item != BarReader.NONE)
                .map(item -> new EquipmentQuantityEmbeddable(
                        item.getCatalogPositionId(),
                        configuration.getQuantity(item)
                ))
                .collect(Collectors.toSet()));
    }

    /**
     * Преобразует LBCEntity в DTO, восстанавливая persistence ID.
     */
    public static LBCDTO toDto(LBCEntity entity) {
        if (entity == null) return null;
        LBCDTO dto = LBCDtoMapper.toDto(toDomain(entity));
        dto.setId(entity.getId());
        return dto;
    }

}