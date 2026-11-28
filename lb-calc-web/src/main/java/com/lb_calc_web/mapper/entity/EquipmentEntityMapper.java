package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.EquipmentPurpose;
import com.lb_calc_web.entity.EquipmentEntity;

import java.util.Objects;

/** Преобразует физические модели оборудования между доменом и JPA. */
public final class EquipmentEntityMapper {

    private EquipmentEntityMapper() {
    }

    /** Восстанавливает доменную модель оборудования из JPA-сущности. */
    public static Equipment toDomain(EquipmentEntity entity) {
        if (entity == null) return null;

        EquipmentFunctionalType functionalType = Objects.requireNonNull(
                entity.getFunctionalType(),
                "Функциональный тип оборудования в БД не должен быть null");
        EquipmentPurpose purpose;
        try {
            purpose = functionalType.parsePurpose(entity.getPurpose());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Некорректное назначение оборудования в БД: " + entity.getPurpose(),
                    exception);
        }

        return new Equipment(
                entity.getId(),
                functionalType,
                purpose,
                entity.getCategory(),
                entity.getName(),
                entity.getDescription(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getDepth()
        );
    }

    /** Создаёт JPA-сущность оборудования из доменной модели. */
    public static EquipmentEntity toEntity(Equipment domain) {
        if (domain == null) return null;

        EquipmentEntity entity = EquipmentEntity.create();
        updateEntity(domain, entity);
        return entity;
    }

    /** Обновляет существующую JPA-сущность оборудования доменными данными. */
    public static void updateEntity(Equipment domain, EquipmentEntity entity) {
        Objects.requireNonNull(domain, "Оборудование не должно быть null");
        Objects.requireNonNull(entity, "EquipmentEntity не должен быть null");

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setFunctionalType(domain.getFunctionalType());
        entity.setPurpose(domain.getPurpose().toString());
        entity.setCategory(domain.getCategory());
        entity.setWidth(domain.getWidth());
        entity.setHeight(domain.getHeight());
        entity.setDepth(domain.getDepth());
    }
}
