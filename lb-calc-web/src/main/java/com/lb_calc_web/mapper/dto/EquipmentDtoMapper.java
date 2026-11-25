package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.*;
import com.lb_calc_web.domain.equipment.Equipment;
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
        EquipmentPurpose purpose = resolvePurpose(type, dto.getPurpose());

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

    private static EquipmentPurpose resolvePurpose(
            EquipmentFunctionalType type,
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Назначение оборудования не должно быть пустым");
        }
        try {
            return switch (type) {
                case DISPLAY -> DisplayPurpose.valueOf(value);
                case ACCESS -> AccessPurpose.valueOf(value);
                case PAYMENT -> PaymentPurpose.valueOf(value);
                case PRINTER -> PrinterPurpose.valueOf(value);
                case SYSTEM -> SystemPurpose.valueOf(value);
            };
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Назначение " + value + " не поддерживается типом " + type,
                    exception
            );
        }
    }
}
