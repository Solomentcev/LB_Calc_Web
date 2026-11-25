package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.equipment.*;
import com.lb_calc_web.entity.EquipmentEntity;

import java.util.Objects;

/** Преобразует физические модели оборудования между доменом и JPA. */
public final class EquipmentEntityMapper {

    private EquipmentEntityMapper() {
    }

    /** Восстанавливает доменную модель оборудования из JPA-сущности. */
    public static Equipment toDomain(EquipmentEntity entity) {
        if (entity == null) return null;

        EquipmentPurpose purpose = resolvePurpose(
                entity.getFunctionalType(), entity.getPurpose());

        Equipment domain = new Equipment(
                entity.getId(),
                entity.getFunctionalType(),
                purpose,
                entity.getCategory(),
                entity.getName(),
                entity.getDescription(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getDepth()
        );
        return domain;
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

    private static EquipmentPurpose resolvePurpose(
            EquipmentFunctionalType functionalType,
            String purpose
    ) {
        Objects.requireNonNull(functionalType, "Функциональный тип оборудования не должен быть null");
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalStateException("Назначение оборудования в БД не должно быть пустым");
        }

        try {
            return switch (functionalType) {
                case DISPLAY -> DisplayPurpose.valueOf(purpose);
                case ACCESS -> AccessPurpose.valueOf(purpose);
                case PAYMENT -> PaymentPurpose.valueOf(purpose);
                case PRINTER -> PrinterPurpose.valueOf(purpose);
                case SYSTEM -> SystemPurpose.valueOf(purpose);
            };
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Назначение " + purpose + " не поддерживается функциональным типом "
                            + functionalType,
                    exception
            );
        }
    }
}
