package com.lb_calc_web.mapper.entity;

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

    /** Восстанавливает модуль с настройками геометрии приложения. */
    public static Module toDomain(ModuleEntity entity, int minimumWidth, int displaySideClearance, int widthRoundingStep,
            int displayCenterHeight, int displayTopClearance) {
        return toDomain(entity, minimumWidth, displaySideClearance, widthRoundingStep,
                displayCenterHeight, displayTopClearance, 200, 200, 20, 1300, 85);
    }

    /** Восстанавливает модуль с настройками геометрии LC и LBC. */
    public static Module toDomain(ModuleEntity entity, int minimumWidth, int displaySideClearance,
            int widthRoundingStep, int displayCenterHeight, int displayTopClearance,
            int lbcTechnicalCellMinWidth, int lbcTechnicalCellMinHeight,
            int lbcEquipmentClearance, int lbcDisplayCenterHeight, int storageCellMinHeight) {
        if (entity == null) {
            return null;
        }

        ModuleEntity concreteEntity = (ModuleEntity) Hibernate.unproxy(entity);
        if (concreteEntity instanceof LBCEntity lbcEntity) {
            return LBCEntityMapper.toDomain(lbcEntity, lbcTechnicalCellMinWidth,
                    lbcTechnicalCellMinHeight, lbcEquipmentClearance,
                    lbcDisplayCenterHeight, storageCellMinHeight);
        }
        if (concreteEntity instanceof LBEntity lbEntity) {
            return LBEntityMapper.toDomain(lbEntity);
        }
        if (concreteEntity instanceof LCEntity lcEntity) {
            return LCEntityMapper.toDomain(lcEntity, minimumWidth, displaySideClearance,
                    widthRoundingStep, displayCenterHeight, displayTopClearance);
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
