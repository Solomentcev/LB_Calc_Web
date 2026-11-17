package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.EquipmentQuantityEmbeddable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Преобразует LC между доменной и persistence-моделями. */
public final class LCEntityMapper {
    private LCEntityMapper() { }

    public static LC toDomain(LCEntity entity) {
        if (entity == null) return null;
        ControlConfiguration configuration = new ControlConfiguration(
                Objects.requireNonNull(entity.getPayment(),
                        "Способ оплаты LC не должен быть null"),
                entity.getAccessMethods(), entity.getPrintOptions(),
                toEquipmentConfiguration(entity));
        LC domain = new LC(
                entity.getHeight(), entity.getWidth(), entity.getDepth(),
                entity.getUpperFrame(), entity.getBottomFrame(),
                Objects.requireNonNull(entity.getColorBody(),
                        "Цвет корпуса LC не должен быть null"),
                Objects.requireNonNull(entity.getColorDoor(),
                        "Цвет дверей LC не должен быть null"),
                configuration);

        if (!domain.getName().equals(entity.getName())
                || !domain.getDescription().equals(entity.getDescription())) {
            throw new IllegalStateException(
                    "Производные характеристики LC в БД не соответствуют исходным параметрам"
            );
        }

        return domain;
    }

    public static LCEntity toEntity(LC domain) {
        if (domain == null) return null;
        LCEntity entity = new LCEntity();
        updateEntity(domain, entity);
        return entity;
    }

    public static void updateEntity(LC domain, LCEntity entity) {
        if (domain == null || entity == null) {
            throw new IllegalArgumentException("LC и LCEntity не должны быть null");
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

        ControlConfiguration configuration = domain.getControlConfiguration();
        entity.setPayment(configuration.getPayment());
        entity.setAccessMethods(configuration.getAccessMethods());
        entity.setPrintOptions(configuration.getPrintOptions());
        updateEquipment(configuration.getEquipmentConfiguration(), entity);
    }

    private static EquipmentConfiguration toEquipmentConfiguration(LCEntity entity) {
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
            LCEntity entity
    ) {
        entity.setEquipment(configuration.getAllEquipment().stream()
                .filter(item -> item != Display.NONE && item != BarReader.NONE)
                .map(item -> new EquipmentQuantityEmbeddable(
                        item.getCatalogPositionId(),
                        configuration.getQuantity(item)
                ))
                .collect(Collectors.toSet()));
    }
}