package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LCGeometryCalculator;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.dto.LCDTO;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
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
        domain.getControlConfiguration().getEquipment().keySet().stream()
                .filter(item -> item.getFunctionalType() == com.lb_calc_web.domain.equipment.EquipmentFunctionalType.DISPLAY)
                .findFirst().ifPresent(display -> {
                    dto.setDisplayWidth(display.getWidth());
                    dto.setDisplayHeight(display.getHeight());
                    dto.setDisplayTop(LCGeometryCalculator.calculateDisplayTop(
                            domain.getHeight(), domain.getUpperFrame(), display.getHeight()));
                });

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

    /** Восстанавливает доменную конфигурацию LC из DTO. */
    public static LC toDomain(LCDTO dto) {
        return toDomain(dto, id -> {
            throw new IllegalStateException(
                    "Для восстановления оборудования требуется загрузчик оборудования");
        }, null);
    }

    /** Восстанавливает доменную конфигурацию LC из DTO. */
    public static LC toDomain(LCDTO dto, Function<Long, Equipment> equipmentLookup) {
        return toDomain(dto, equipmentLookup, null);
    }

    /** Восстанавливает доменную конфигурацию LC из DTO. */
    public static LC toDomain(
            LCDTO dto,
            Function<Long, Equipment> equipmentLookup,
            ControlConfiguration fallbackConfiguration
    ) {
        if (dto == null) {
            return null;
        }
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");

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

        ControlConfiguration configuration = fallbackConfiguration == null
                ? new ControlConfiguration(functional, Map.of())
                : fallbackConfiguration.copy();
        configuration.setFunctionalConfiguration(functional);
        if (dto.getEquipmentSelections() != null || dto.isEquipmentSelectionsProvided()
                || fallbackConfiguration == null) {
            configuration.setEquipment(equipment);
        }

        LC domain = new LC(
                dto.getHeight(),
                dto.getWidth(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor()),
                configuration
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

    /** Преобразует список доменных конфигураций LC в список DTO. */
    public static List<LCDTO> toDtoList(List<LC> domains) {
        return domains == null ? List.of() : domains.stream().map(LCDtoMapper::toDto).toList();
    }
}
