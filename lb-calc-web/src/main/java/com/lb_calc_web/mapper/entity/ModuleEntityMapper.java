package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.function.Function;

/**
 * Преобразует полиморфную иерархию модулей между доменной
 * и persistence-моделями.
 *
 * <p>Общие поля базовой сущности и параметры конкретного типа
 * преобразуются специализированными мапперами.</p>
 */
public final class ModuleEntityMapper {

    private ModuleEntityMapper() {
    }

    /**
     * Преобразует конкретную JPA-сущность модуля в доменную модель.
     *
     * @param entity JPA-сущность модуля
     * @return доменная модель или null, если передан null
     */
    public static Module toDomain(ModuleEntity entity) {
        return toDomain(entity, EquipmentCatalog::findByCatalogPositionId);
    }

    public static Module toDomain(ModuleEntity entity,
                                  Function<String, Equipment> equipmentResolver) {
        if (entity == null) {
            return null;
        }
        Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");

        ModuleEntity concreteEntity =
                (ModuleEntity) Hibernate.unproxy(entity);

        if (concreteEntity instanceof LBCEntity lbcEntity) {
            return LBCEntityMapper.toDomain(lbcEntity, equipmentResolver);
        }

        if (concreteEntity instanceof LBEntity lbEntity) {
            return LBEntityMapper.toDomain(lbEntity);
        }

        if (concreteEntity instanceof LCEntity lcEntity) {
            return LCEntityMapper.toDomain(lcEntity, equipmentResolver);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип ModuleEntity: "
                        + concreteEntity.getClass().getName()
        );
    }

    /**
     * Создаёт новую JPA-сущность соответствующего типа модуля.
     *
     * @param domain доменная модель модуля
     * @return новая JPA-сущность без persistence-идентификатора
     */
    public static ModuleEntity toEntity(Module domain) {
        Objects.requireNonNull(
                domain,
                "Доменный модуль не должен быть null"
        );

        if (domain instanceof LBC lbc) {
            return LBCEntityMapper.toEntity(lbc);
        }

        if (domain instanceof LB lb) {
            return LBEntityMapper.toEntity(lb);
        }

        if (domain instanceof LC lc) {
            return LCEntityMapper.toEntity(lc);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип Module: "
                        + domain.getClass().getName()
        );
    }
}
