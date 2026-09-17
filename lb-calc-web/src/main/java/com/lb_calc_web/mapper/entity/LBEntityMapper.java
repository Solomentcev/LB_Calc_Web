package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.entity.LBEntity;

import java.util.Objects;

/**
 * Маппер между LBEntity и доменной моделью LB.
 *
 * <p>Отвечает только за преобразование persistence-модели
 * в domain-модель и обратно.</p>
 */
public final class LBEntityMapper {

    private LBEntityMapper() {
    }

    /**
     * Entity -> Domain.
     *
     * <p>Производные значения Domain не берутся из БД,
     * а пересчитываются конструктором LB.</p>
     */
    public static LB toDomain(LBEntity entity) {
        if (entity == null) {
            return null;
        }

        TypeLb typeLb = new TypeLb(
                entity.getType(),
                entity.getDeltaWidth(),
                entity.getShelfThick(),
                entity.getServiceZoneWidth()
        );

        return new LB(
                entity.getHeight(),
                entity.getWidth(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                Objects.requireNonNull(
                        entity.getColorBody(),
                        "Цвет корпуса LB не должен быть null"
                ),
                Objects.requireNonNull(
                        entity.getColorDoor(),
                        "Цвет дверей LB не должен быть null"
                ),
                typeLb,
                entity.getCountCells(),
                Objects.requireNonNull(
                        entity.getDirectionDoorOpening(),
                        "Направление открытия дверей LB не должно быть null"
                ),
                entity.getDoorThickness()
        );
    }

    /**
     * Domain -> новая Entity.
     *
     * <p>Идентификатор не устанавливается, поскольку Domain
     * не содержит persistence-id.</p>
     */
    public static LBEntity toEntity(LB domain) {
        if (domain == null) {
            return null;
        }

        LBEntity entity = new LBEntity();

        updateEntity(domain, entity);

        return entity;
    }

    /**
     * Обновляет существующую Entity данными Domain.
     *
     * <p>Идентификатор существующей Entity сохраняется.</p>
     */
    public static void updateEntity(
            LB domain,
            LBEntity entity
    ) {
        if (domain == null) {
            throw new IllegalArgumentException(
                    "LB не должен быть null"
            );
        }

        if (entity == null) {
            throw new IllegalArgumentException(
                    "LBEntity не должен быть null"
            );
        }

        /*
         * Общие параметры ModuleEntity.
         */
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        entity.setHeight(domain.getHeight());
        entity.setWidth(domain.getWidth());
        entity.setDepth(domain.getDepth());

        entity.setUpperFrame(domain.getUpperFrame());
        entity.setBottomFrame(domain.getBottomFrame());

        entity.setColorBody(domain.getColorBody());
        entity.setColorDoor(domain.getColorDoor());

        /*
         * TypeLb хранится в Entity как набор простых полей.
         */
        TypeLb typeLb = domain.getTypeLb();

        entity.setType(typeLb.getType());
        entity.setDeltaWidth(typeLb.getDeltaWidth());
        entity.setShelfThick(typeLb.getShelfThick());
        entity.setServiceZoneWidth(typeLb.getServiceZoneWidth());

        /*
         * Параметры конкретного storage-модуля.
         */
        entity.setDoorThickness(domain.getDoorThickness());
        entity.setDirectionDoorOpening(
                domain.getDirectionDoorOpening()
        );

        entity.setCountCells(domain.getCountCells());

        /*
         * Производные размеры ячеек сохраняем в БД,
         * чтобы Entity отражала состояние Domain.
         */
        entity.setHeightCell(domain.getCellHeight());
        entity.setWidthCell(domain.getCellWidth());
        entity.setDepthCell(domain.getCellDepth());
    }
}