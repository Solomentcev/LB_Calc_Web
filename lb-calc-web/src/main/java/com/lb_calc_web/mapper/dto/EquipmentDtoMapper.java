package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.EquipmentPurpose;
import com.lb_calc_web.dto.EquipmentDTO;

import java.util.Objects;

/** Преобразует физические модели оборудования между DTO и доменом. */
public final class EquipmentDtoMapper {

    private EquipmentDtoMapper() {
    }

    /** Преобразует DTO оборудования в доменную модель. */
    public static Equipment toDomain(EquipmentDTO dto, Long id) {
        Objects.requireNonNull(dto, "DTO оборудования не должен быть null");
        EquipmentFunctionalType type = Objects.requireNonNull(
                dto.getFunctionalType(), "Функциональный тип оборудования не должен быть null");
        EquipmentPurpose purpose = type.parsePurpose(dto.getPurpose());

        return new Equipment(
                id,
                type,
                purpose,
                dto.getCategory(),
                dto.getName(),
                dto.getDescription(),
                dto.getWidth(),
                dto.getHeight(),
                dto.getDepth()
        );
    }

    /** Преобразует доменную модель оборудования в DTO. */
    public static EquipmentDTO toDto(Equipment equipment) {
        if (equipment == null) {
            return null;
        }

        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setFunctionalType(equipment.getFunctionalType());
        dto.setPurpose(equipment.getPurpose().toString());
        dto.setCategory(equipment.getCategory());
        dto.setName(equipment.getName());
        dto.setDescription(equipment.getDescription());
        dto.setWidth(equipment.getWidth());
        dto.setHeight(equipment.getHeight());
        dto.setDepth(equipment.getDepth());
        return dto;
    }
}
