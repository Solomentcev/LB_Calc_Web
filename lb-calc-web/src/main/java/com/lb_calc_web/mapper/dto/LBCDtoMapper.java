package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.service.validation.size.SizeBounds;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.dto.LBCDTO;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
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
        dto.setFunctionalConfiguration(toDto(configuration.getFunctionalConfiguration()));
        dto.setEquipmentSelections(configuration.getEquipment().entrySet().stream()
                .map(entry -> new EquipmentSelectionDTO(entry.getKey().getId(), entry.getValue()))
                .toList());
        return dto;
    }

    private static FunctionalConfigurationDTO toDto(FunctionalConfiguration configuration) {
        FunctionalConfigurationDTO dto = new FunctionalConfigurationDTO();
        dto.setDisplay(configuration.getDisplay());
        dto.setAccess(configuration.getAccess());
        dto.setPayment(configuration.getPayment());
        dto.setPrinter(configuration.getPrinter());
        return dto;
    }

    /** Восстанавливает LBC с заданными ограничениями физической геометрии. */
    public static LBC toDomain(
            LBCDTO dto,
            TypeLb typeLb,
            Function<Long, Equipment> equipmentLookup,
            ControlConfiguration fallbackConfiguration,
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

        FunctionalConfiguration functional = toDomain(
                dto.getFunctionalConfiguration(), fallbackConfiguration);

        Map<Equipment, Integer> equipment = new LinkedHashMap<>();
        if (dto.getEquipmentSelections() == null && !dto.isEquipmentSelectionsProvided()
                && fallbackConfiguration != null) {
            equipment.putAll(fallbackConfiguration.getEquipment());
        } else if (dto.getEquipmentSelections() != null) {
            for (EquipmentSelectionDTO selection : dto.getEquipmentSelections()) {
                Objects.requireNonNull(selection, "Позиция оборудования не должна быть null");
                if (selection.getEquipmentId() == null) {
                    if (dto.isEquipmentSelectionsProvided()) {
                        continue;
                    }
                    throw new IllegalArgumentException(
                            "Идентификатор модели оборудования не должен быть null");
                }
                Equipment model = Objects.requireNonNull(
                        equipmentLookup.apply(selection.getEquipmentId()),
                        "Модель оборудования не найдена: " + selection.getEquipmentId()
                );
                equipment.merge(model, selection.getQuantity(), Math::addExact);
            }
        }

        ControlConfiguration configuration = new ControlConfiguration(functional, equipment);

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

    private static FunctionalConfiguration toDomain(
            FunctionalConfigurationDTO dto,
            ControlConfiguration fallback
    ) {
        boolean provided = dto != null && (dto.getDisplay() != null
                || dto.getAccess() != null
                || dto.getPayment() != null
                || dto.getPrinter() != null);
        if (!provided && fallback != null) {
            return fallback.getFunctionalConfiguration();
        }
        return new FunctionalConfiguration(
                dto == null || dto.getDisplay() == null
                        ? com.lb_calc_web.domain.equipment.DisplayPurpose.NONE : dto.getDisplay(),
                dto == null || dto.getAccess() == null ? Set.of() : dto.getAccess(),
                dto == null || dto.getPayment() == null
                        ? com.lb_calc_web.domain.equipment.PaymentPurpose.NONE : dto.getPayment(),
                dto == null || dto.getPrinter() == null
                        ? com.lb_calc_web.domain.equipment.PrinterPurpose.ABSENT : dto.getPrinter()
        );
    }

    /** Преобразует список доменных конфигураций LBC в список DTO. */
    public static List<LBCDTO> toDtoList(List<LBC> domains) {
        return domains == null ? List.of() : domains.stream().map(LBCDtoMapper::toDto).toList();
    }
}
