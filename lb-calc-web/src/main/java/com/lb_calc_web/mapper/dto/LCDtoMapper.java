package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LCDTO;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Преобразует LC между транспортной и доменной моделями. */
public final class LCDtoMapper {

    private LCDtoMapper() {
    }

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
        FunctionalConfiguration functional = configuration.getFunctionalConfiguration();
        dto.setDisplayPurpose(functional.getDisplay().name());
        dto.setAccessPurposes(functional.getAccess().stream().map(Enum::name).sorted().toList());
        dto.setPaymentPurpose(functional.getPayment().name());
        dto.setPrinterPurpose(functional.getPrinter().name());
        dto.setEquipmentSelections(configuration.getEquipment().entrySet().stream()
                .map(entry -> new EquipmentSelectionDTO(
                        entry.getKey().getId(), entry.getValue()))
                .toList());
        return dto;
    }

    public static LC toDomain(LCDTO dto) {
        return toDomain(dto, id -> {
            throw new IllegalStateException(
                    "Для восстановления оборудования требуется Equipment resolver");
        }, null);
    }

    public static LC toDomain(LCDTO dto, Function<Long, Equipment> equipmentResolver) {
        return toDomain(dto, equipmentResolver, null);
    }

    public static LC toDomain(
            LCDTO dto,
            Function<Long, Equipment> equipmentResolver,
            ControlConfiguration fallbackConfiguration
    ) {
        if (dto == null) {
            return null;
        }
        Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");

        boolean functionalConfigurationProvided =
                dto.getDisplayPurpose() != null
                        || dto.getPaymentPurpose() != null
                        || dto.getPrinterPurpose() != null
                        || dto.getAccessPurposes() != null;

        FunctionalConfiguration functional = !functionalConfigurationProvided
                && fallbackConfiguration != null
                ? fallbackConfiguration.getFunctionalConfiguration()
                : new FunctionalConfiguration(
                        parseDisplayPurpose(dto.getDisplayPurpose()),
                        parseAccessPurposes(dto.getAccessPurposes()),
                        parsePaymentPurpose(dto.getPaymentPurpose()),
                        parsePrinterPurpose(dto.getPrinterPurpose())
                );

        Map<Equipment, Integer> equipment = new LinkedHashMap<>();
        if (dto.getEquipmentSelections() == null && fallbackConfiguration != null) {
            equipment.putAll(fallbackConfiguration.getEquipment());
        } else if (dto.getEquipmentSelections() != null) {
            for (EquipmentSelectionDTO selection : dto.getEquipmentSelections()) {
                Objects.requireNonNull(selection, "Позиция оборудования не должна быть null");
                Equipment model = Objects.requireNonNull(
                        equipmentResolver.apply(selection.getEquipmentId()),
                        "Модель оборудования не найдена: " + selection.getEquipmentId()
                );
                equipment.merge(model, selection.getQuantity(), Math::addExact);
            }
        }

        LC domain = new LC(
                dto.getHeight(),
                dto.getWidth(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor()),
                new ControlConfiguration(functional, equipment)
        );
        domain.setId(dto.getId() == null || dto.getId() <= 0 ? null : dto.getId());
        return domain;
    }

    private static DisplayPurpose parseDisplayPurpose(String value) {
        return value == null || value.isBlank()
                ? DisplayPurpose.NONE : DisplayPurpose.valueOf(value);
    }

    private static java.util.Set<AccessPurpose> parseAccessPurposes(List<String> values) {
        EnumSet<AccessPurpose> result = EnumSet.noneOf(AccessPurpose.class);
        if (values != null) {
            values.stream().filter(Objects::nonNull).filter(value -> !value.isBlank())
                    .map(AccessPurpose::valueOf).forEach(result::add);
        }
        return result;
    }

    private static PaymentPurpose parsePaymentPurpose(String value) {
        return value == null || value.isBlank()
                ? PaymentPurpose.NONE : PaymentPurpose.valueOf(value);
    }

    private static PrinterPurpose parsePrinterPurpose(String value) {
        return value == null || value.isBlank()
                ? PrinterPurpose.ABSENT : PrinterPurpose.valueOf(value);
    }

    public static List<LCDTO> toDtoList(List<LC> domains) {
        return domains == null ? List.of() : domains.stream().map(LCDtoMapper::toDto).toList();
    }
}
