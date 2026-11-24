package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.EquipmentQuantityEmbeddable;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Преобразует LBC между доменной и persistence-моделями. */
public final class LBCEntityMapper {

    private LBCEntityMapper() {
    }

    public static LBC toDomain(LBCEntity entity) {
        if (entity == null) {
            return null;
        }

        Map<Equipment, Integer> equipment = entity.getEquipment().stream()
                .collect(Collectors.toMap(
                        item -> EquipmentEntityMapper.toDomain(item.getEquipment()),
                        EquipmentQuantityEmbeddable::getQuantity,
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Модель оборудования LBC сохранена в нескольких позициях");
                        },
                        LinkedHashMap::new
                ));

        TypeLb typeLb = new TypeLb(
                entity.getType(),
                entity.getDeltaWidth(),
                entity.getShelfThick(),
                entity.getServiceZoneWidth()
        );
        ControlConfiguration configuration = new ControlConfiguration(
                entity.getFunctionalConfiguration(),
                equipment
        );
        LBC domain = new LBC(
                entity.getHeight(),
                entity.getWidth(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                Objects.requireNonNull(entity.getColorBody(),
                        "Цвет корпуса LBC не должен быть null"),
                Objects.requireNonNull(entity.getColorDoor(),
                        "Цвет дверей LBC не должен быть null"),
                typeLb,
                entity.getCountCells(),
                Objects.requireNonNull(entity.getDirectionDoorOpening(),
                        "Направление открытия дверей LBC не должно быть null"),
                entity.getDoorThickness(),
                configuration
        );

        // Размеры ячеек, наименование и описание пересчитываются доменной моделью.
        domain.setId(entity.getId());
        return domain;
    }

    public static LBCEntity toEntity(
            LBC domain,
            Function<Equipment, EquipmentEntity> equipmentLookup
    ) {
        if (domain == null) {
            return null;
        }
        LBCEntity entity = LBCEntity.create();
        updateEntity(domain, entity, equipmentLookup);
        return entity;
    }

    public static void updateEntity(
            LBC domain,
            LBCEntity entity,
            Function<Equipment, EquipmentEntity> equipmentLookup
    ) {
        if (domain == null || entity == null) {
            throw new IllegalArgumentException("LBC и LBCEntity не должны быть null");
        }
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");

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
        entity.setFunctionalConfiguration(configuration.getFunctionalConfiguration());
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

    /** Преобразует LBCEntity в DTO, сохраняя persistence ID. */
    public static LBCDTO toDto(LBCEntity entity) {
        if (entity == null) {
            return null;
        }
        LBCDTO dto = LBCDtoMapper.toDto(toDomain(entity));
        dto.setId(entity.getId());
        return dto;
    }
}
