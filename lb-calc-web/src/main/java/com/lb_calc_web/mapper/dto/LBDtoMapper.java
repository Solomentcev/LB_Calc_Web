package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.dto.LBDTO;

import java.util.Objects;

/** Преобразует доменную модель LB в DTO и восстанавливает её из DTO. */
public final class LBDtoMapper {

    private LBDtoMapper() {
    }

    /** Преобразует доменную модель LB в DTO. */
    public static LBDTO toDto(LB domain) {
        if (domain == null) {
            return null;
        }

        LBDTO dto = new LBDTO();
        dto.setId(domain.getId());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setHeight(domain.getHeight());
        dto.setWidth(domain.getWidth());
        dto.setDepth(domain.getDepth());
        dto.setUpperFrame(domain.getUpperFrame());
        dto.setBottomFrame(domain.getBottomFrame());
        dto.setCountCells(domain.getCountCells());
        dto.setHeightCell(domain.getCellHeight());
        dto.setWidthCell(domain.getCellWidth());
        dto.setDepthCell(domain.getCellDepth());
        dto.setDirectionDoorOpening(domain.getDirectionDoorOpening().name());
        dto.setColorBody(domain.getColorBody().name());
        dto.setColorDoor(domain.getColorDoor().name());

        TypeLb typeLb = domain.getTypeLb();
        dto.setType(typeLb.getType());
        dto.setShelfThick(typeLb.getShelfThick());
        dto.setDeltaWidth(typeLb.getDeltaWidth());
        dto.setServiceZoneWidth(typeLb.getServiceZoneWidth());
        dto.setDoorThickness(domain.getDoorThickness());
        return dto;
    }

    /**
     * Преобразует DTO в доменную модель с конструктивным типом,
     * разрешённым application-слоем.
     */
    public static LB toDomain(LBDTO dto, TypeLb typeLb) {
        if (dto == null) {
            return null;
        }

        Objects.requireNonNull(typeLb, "Тип LB не должен быть null");
        if (!Objects.equals(dto.getType(), typeLb.getType())) {
            throw new IllegalArgumentException(
                    "Переданный конструктивный тип не соответствует типу DTO LB");
        }

        LB domain = new LB(
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
                dto.getDoorThickness()
        );
        domain.setId(dto.getId() == null || dto.getId() <= 0 ? null : dto.getId());
        return domain;
    }
}
