package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.dto.LBDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**

 * Маппер между Domain LB и LBDTO.
 */
public final class LBDtoMapper {

    private LBDtoMapper() {
    }

    /**

     * Domain -> DTO.
     */
    public static LBDTO toDto(LB domain) {
        if (domain == null) {
            return null;
        }

        LBDTO dto = new LBDTO();

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

        dto.setDirectionDoorOpening(
                domain.getDirectionDoorOpening().name()
        );

        dto.setColorBody(
                domain.getColorBody().name()
        );

        dto.setColorDoor(
                domain.getColorDoor().name()
        );

        TypeLb typeLb = domain.getTypeLb();

        dto.setType(typeLb.getType());
        dto.setShelfThick(typeLb.getShelfThick());
        dto.setDeltaWidth(typeLb.getDeltaWidth());
        dto.setServiceZoneWidth(typeLb.getServiceZoneWidth());

        dto.setDoorThickness(
                domain.getDoorThickness()
        );

        return dto;
    }

    /**

     * DTO -> Domain.
     */
    public static LB toDomain(LBDTO dto) {
        if (dto == null) {
            return null;
        }

        TypeLb typeLb = new TypeLb(
                Objects.requireNonNull(
                        dto.getType(),
                        "Тип LB не должен быть null"
                ),
                dto.getDeltaWidth(),
                dto.getShelfThick(),
                dto.getServiceZoneWidth()
        );

        return new LB(
                dto.getHeight(),
                dto.getWidth(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor()),
                typeLb,
                dto.getCountCells(),
                DirectionDoorOpening.valueOf(
                        dto.getDirectionDoorOpening()
                ),
                dto.getDoorThickness()
        );
    }

    /**

     * Domain -> DTO.
     */
    public static List<LBDTO> toDtoList(
            List<LB> domains
    ) {
        if (domains == null) {
            return List.of();
        }

        List<LBDTO> result = new ArrayList<>(
                domains.size()
        );

        for (LB domain : domains) {
            result.add(toDto(domain));
        }

        return result;
    }

    /**

     * DTO -> Domain.
     */
    public static List<LB> toDomainList(
            List<LBDTO> dtos
    ) {
        if (dtos == null) {
            return List.of();
        }

        List<LB> result = new ArrayList<>(
                dtos.size()
        );

        for (LBDTO dto : dtos) {
            result.add(toDomain(dto));
        }

        return result;
    }
}
