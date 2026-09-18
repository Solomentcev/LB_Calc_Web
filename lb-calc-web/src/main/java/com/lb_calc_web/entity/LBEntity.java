package com.lb_calc_web.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * JPA-сущность модуля хранения LB.
 *
 * <p>Все общие параметры модуля хранения наследуются
 * от {@link StorageModuleEntity}.</p>
 *
 * <p>Бизнес-логика расчёта размеров, ячеек и описания
 * находится в domain-модели {@code LB}.</p>
 *
 * <p>Связь LB с ALS не хранится непосредственно в этой сущности.
 * Для связи с конкретной позицией внутри ALS используется
 * {@code ALSModuleEntity}.</p>
 */
@Entity
@Table(name = "lb")
public class LBEntity extends StorageModuleEntity {

    /**
     * Конструктор для JPA.
     */
    public LBEntity() {
    }
}