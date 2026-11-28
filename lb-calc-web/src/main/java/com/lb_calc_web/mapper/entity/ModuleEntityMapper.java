package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.service.validation.size.SizeBounds;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.function.Function;

/** Преобразует полиморфную иерархию модулей между доменной и persistence-моделями. */
public final class ModuleEntityMapper {

    private ModuleEntityMapper() {
    }

    /** Восстанавливает модуль с настройками геометрии LC и LBC. */
    public static Module toDomain(ModuleEntity entity, SizeBounds bounds) {
        if (entity == null) {
            return null;
        }

        ModuleEntity concreteEntity = (ModuleEntity) Hibernate.unproxy(entity);
        if (concreteEntity instanceof LBCEntity lbcEntity) {
            return LBCEntityMapper.toDomain(lbcEntity, bounds);
        }
        if (concreteEntity instanceof LBEntity lbEntity) {
            return LBEntityMapper.toDomain(lbEntity, bounds);
        }
        if (concreteEntity instanceof LCEntity lcEntity) {
            return LCEntityMapper.toDomain(lcEntity, bounds);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип ModuleEntity: " + concreteEntity.getClass().getName()
        );
    }

    /** Создаёт общую JPA-сущность модуля из доменной модели. */
    public static ModuleEntity toEntity(
            Module domain,
            Function<Equipment, EquipmentEntity> equipmentLookup
    ) {
        Objects.requireNonNull(domain, "Доменный модуль не должен быть null");
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");

        if (domain instanceof LBC lbc) {
            return LBCEntityMapper.toEntity(lbc, equipmentLookup);
        }
        if (domain instanceof LB lb) {
            return LBEntityMapper.toEntity(lb);
        }
        if (domain instanceof LC lc) {
            return LCEntityMapper.toEntity(lc, equipmentLookup);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип Module: " + domain.getClass().getName()
        );
    }
}
