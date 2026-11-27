package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.geometry.SizeBounds;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.dto.LCDTO;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Преобразует LC между транспортной и доменной моделями. */
public final class LCDtoMapper {

    private LCDtoMapper() {
    }

    /** Преобразует доменную конфигурацию LC в DTO. */
    public static LCDTO toDto(LC domain) {
        if (domain == null) {
            return null;
        }

        LCDTO dto = new LCDTO();
        dto.setId(domain.getId());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setHeight(domain.getHeight());
        dto.setWidth(domain.getWidth());
        dto.setDepth(domain.getDepth());
        dto.setUpperFrame(domain.getUpperFrame());
        dto.setBottomFrame(domain.getBottomFrame());
        dto.setColorBody(domain.getColorBody().name());
        dto.setColorDoor(domain.getColorDoor().name());
        ControlConfiguration configuration = domain.getControlConfiguration();
        dto.setFunctionalConfiguration(ControlConfigurationDtoMapper.toFunctionalDto(
                configuration.getFunctionalConfiguration()));
                dto.setSelectedEquipment(ControlConfigurationDtoMapper.toEquipmentDetails(configuration));
        return dto;
    }

    /** Восстанавливает LC с настройками геометрии приложения. */
    public static LC toDomain(
            LCDTO dto,
            Function<Long, Equipment> equipmentLookup,
            SizeBounds bounds
    ) {
        if (dto == null) {
            return null;
        }
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");

        ControlConfiguration configuration = ControlConfigurationDtoMapper.toDomain(
                dto.getFunctionalConfiguration(),
                dto.getSelectedEquipment(),
                dto.isEquipmentProvided(),
                equipmentLookup
        );

        LC domain = new LC(
                dto.getHeight(),
                dto.getWidth(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor()),
                configuration,
                bounds
        );
        domain.setId(dto.getId() == null || dto.getId() <= 0 ? null : dto.getId());
        return domain;
    }

    /** Преобразует список доменных конфигураций LC в список DTO. */
    public static List<LCDTO> toDtoList(List<LC> domains) {
        return domains == null ? List.of() : domains.stream().map(LCDtoMapper::toDto).toList();
    }
}
