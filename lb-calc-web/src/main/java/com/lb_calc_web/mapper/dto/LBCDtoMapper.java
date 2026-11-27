package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.geometry.SizeBounds;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.dto.LBCDTO;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Преобразует LBC между транспортной и доменной моделями. */
public final class LBCDtoMapper {

    private LBCDtoMapper() {
    }

    /** Преобразует доменную конфигурацию LBC в DTO. */
    public static LBCDTO toDto(LBC domain) {
        if (domain == null) {
            return null;
        }

        LBCDTO dto = new LBCDTO();
        dto.setId(domain.getId());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setType(domain.getTypeLb().getType());
        dto.setDeltaWidth(domain.getTypeLb().getDeltaWidth());
        dto.setShelfThick(domain.getTypeLb().getShelfThick());
        dto.setServiceZoneWidth(domain.getTypeLb().getServiceZoneWidth());
        dto.setHeight(domain.getHeight());
        dto.setWidth(domain.getWidth());
        dto.setDepth(domain.getDepth());
        dto.setUpperFrame(domain.getUpperFrame());
        dto.setBottomFrame(domain.getBottomFrame());
        dto.setCountCells(domain.getCountCells());
        dto.setHeightCell(domain.getCellHeight());
        dto.setWidthCell(domain.getCellWidth());
        dto.setDepthCell(domain.getCellDepth());
        dto.setDoorThickness(domain.getDoorThickness());
        dto.setDirectionDoorOpening(domain.getDirectionDoorOpening().name());
        dto.setColorBody(domain.getColorBody().name());
        dto.setColorDoor(domain.getColorDoor().name());
        ControlConfiguration configuration = domain.getControlConfiguration();
        dto.setFunctionalConfiguration(ControlConfigurationDtoMapper.toFunctionalDto(
                configuration.getFunctionalConfiguration()));
                dto.setSelectedEquipment(ControlConfigurationDtoMapper.toEquipmentDetails(configuration));
        return dto;
    }

    /** Восстанавливает LBC с заданными ограничениями физической геометрии. */
    public static LBC toDomain(
            LBCDTO dto,
            TypeLb typeLb,
            Function<Long, Equipment> equipmentLookup,
            SizeBounds bounds
    ) {
        if (dto == null) {
            return null;
        }
        Objects.requireNonNull(typeLb, "Тип LBC не должен быть null");
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");
        if (!Objects.equals(dto.getType(), typeLb.getType())) {
            throw new IllegalArgumentException(
                    "Переданный конструктивный тип не соответствует типу DTO LBC");
        }

        ControlConfiguration configuration = ControlConfigurationDtoMapper.toDomain(
                dto.getFunctionalConfiguration(),
                dto.getSelectedEquipment(),
                dto.isEquipmentProvided(),
                equipmentLookup
        );

        LBC domain = new LBC(
                dto.getHeight(),
                dto.getWidth(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor()),
                typeLb,
                dto.getCountCells(),
                DirectionDoorOpening.valueOf(dto.getDirectionDoorOpening()),
                dto.getDoorThickness(),
                configuration,
                bounds
        );
        domain.setId(dto.getId() == null || dto.getId() <= 0 ? null : dto.getId());
        return domain;
    }

    /** Преобразует список доменных конфигураций LBC в список DTO. */
    public static List<LBCDTO> toDtoList(List<LBC> domains) {
        return domains == null ? List.of() : domains.stream().map(LBCDtoMapper::toDto).toList();
    }
}
