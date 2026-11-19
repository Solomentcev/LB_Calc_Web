package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;

import java.util.List;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;

import java.util.Objects;
import java.util.function.Function;

/**
 * Преобразует DTO ALS в доменную модель с переданными конфигурациями типов модулей.
 */
public final class ALSConfigurationMapper {

    private ALSConfigurationMapper() {
    }

    /**
     * @param dto DTO конфигурации ALS
     * @param lbTypeResolver разрешает конструктивный тип LB
     * @param lbcTypeResolver разрешает конструктивный тип LBC
     * @return доменная конфигурация ALS
     */
    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver
    ) {
        Objects.requireNonNull(lbTypeResolver, "lbTypeResolver не должен быть null");
        Objects.requireNonNull(lbcTypeResolver, "lbcTypeResolver не должен быть null");
        return ALSDtoMapper.toDomain(dto, lbTypeResolver, lbcTypeResolver, EquipmentCatalog::resolve);
    }

    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<String, Equipment> equipmentResolver
    ) {
        return toDomain(
                dto,
                lbTypeResolver,
                lbcTypeResolver,
                equipmentResolver,
                dto == null || dto.getLC() == null ? null : dto.getLC().getEquipmentSelections(),
                dto == null || dto.getLBC() == null ? null : dto.getLBC().getEquipmentSelections()
        );
    }

    /**
     * Преобразует ALS с подготовленным составом оборудования управляющего модуля.
     */
    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<String, Equipment> equipmentResolver,
            List<EquipmentSelectionDTO> lcEquipmentSelections,
            List<EquipmentSelectionDTO> lbcEquipmentSelections
    ) {
        Objects.requireNonNull(lbTypeResolver, "lbTypeResolver не должен быть null");
        Objects.requireNonNull(lbcTypeResolver, "lbcTypeResolver не должен быть null");
        Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");
        return ALSDtoMapper.toDomain(
                dto,
                lbTypeResolver,
                lbcTypeResolver,
                equipmentResolver,
                lcEquipmentSelections,
                lbcEquipmentSelections
        );
    }
}
