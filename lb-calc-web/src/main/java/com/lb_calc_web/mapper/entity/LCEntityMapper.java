package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.geometry.SizeBounds;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.entity.EquipmentQuantityEmbeddable;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.LCEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Преобразует LC между доменной и persistence-моделями. */
public final class LCEntityMapper {

    private LCEntityMapper() {
    }

    /** Восстанавливает LC с настройками геометрии приложения. */
    public static LC toDomain(LCEntity entity, SizeBounds bounds) {
        if (entity == null) {
            return null;
        }

        Map<Equipment, Integer> equipment = entity.getEquipment().stream()
                .collect(Collectors.toMap(
                        item -> EquipmentEntityMapper.toDomain(item.getEquipment()),
                        EquipmentQuantityEmbeddable::getQuantity,
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Модель оборудования LC сохранена в нескольких позициях");
                        },
                        LinkedHashMap::new
                ));

        ControlConfiguration configuration = new ControlConfiguration(
                new FunctionalConfiguration(
                        entity.getDisplayPurpose(), entity.getAccessPurposes(),
                        entity.getPaymentPurpose(), entity.getPrinterPurpose()),
                equipment
        );
        LC domain = new LC(
                entity.getHeight(),
                entity.getWidth(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                Objects.requireNonNull(entity.getColorBody(),
                        "Цвет корпуса LC не должен быть null"),
                Objects.requireNonNull(entity.getColorDoor(),
                        "Цвет дверей LC не должен быть null"),
                configuration,
                bounds
        );

        domain = (LC) Module.restoreText(domain, entity.getName(), entity.getDescription());
        domain.setId(entity.getId());
        return domain;
    }

    /** Создаёт JPA-сущность LC из доменной конфигурации. */
    public static LCEntity toEntity(
            LC domain,
            Function<Equipment, EquipmentEntity> equipmentLookup
    ) {
        if (domain == null) {
            return null;
        }
        LCEntity entity = LCEntity.create();
        updateEntity(domain, entity, equipmentLookup);
        return entity;
    }

    /** Обновляет существующую JPA-сущность LC доменными данными. */
    public static void updateEntity(
            LC domain,
            LCEntity entity,
            Function<Equipment, EquipmentEntity> equipmentLookup
    ) {
        if (domain == null || entity == null) {
            throw new IllegalArgumentException("LC и LCEntity не должны быть null");
        }
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");

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
        FunctionalConfiguration functional = configuration.getFunctionalConfiguration();
        entity.setDisplayPurpose(functional.getDisplay());
        entity.setAccessPurposes(functional.getAccess());
        entity.setPaymentPurpose(functional.getPayment());
        entity.setPrinterPurpose(functional.getPrinter());
        Set<EquipmentQuantityEmbeddable> equipment = configuration.getEquipment().entrySet()
                .stream()
                .map(entry -> new EquipmentQuantityEmbeddable(
                        Objects.requireNonNull(
                                equipmentLookup.apply(entry.getKey()),
                                "equipmentLookup вернул null"
                        ),
                        entry.getValue()
                ))
                .collect(Collectors.toSet());
        entity.setEquipment(equipment);
    }


}
