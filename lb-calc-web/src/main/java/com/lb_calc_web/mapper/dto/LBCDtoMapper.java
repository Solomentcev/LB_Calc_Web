package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LBCDTO;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Преобразует LBC между транспортной и доменной моделями. */
public final class LBCDtoMapper {

    private LBCDtoMapper() {
    }

    private static int quantityForCategory(
            ControlConfiguration configuration,
            EquipmentCategory category
    ) {
        return configuration.getEquipment().entrySet().stream()
                .filter(entry -> entry.getKey().getCategory() == category)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

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
        FunctionalConfiguration functional = configuration.getFunctionalConfiguration();
        dto.setDisplayPurpose(functional.getDisplay().name());
        dto.setAccessPurposes(functional.getAccess().stream().map(Enum::name).sorted().toList());
        dto.setPaymentPurpose(functional.getPayment().name());
        dto.setPrinterPurpose(functional.getPrinter().name());

        int displayQuantity = quantityForCategory(configuration, EquipmentCategory.DISPLAY);
        int barReaderQuantity = quantityForCategory(configuration, EquipmentCategory.BAR_READER);
        int printerQuantity = quantityForCategory(configuration, EquipmentCategory.PRINTER);
        int rfidReaderQuantity = quantityForCategory(configuration, EquipmentCategory.RFID_READER);
        dto.setDisplayQuantity(displayQuantity);
        dto.setBarReaderQuantity(barReaderQuantity);
        dto.setPrinterQuantity(printerQuantity);
        dto.setRfidReaderQuantity(rfidReaderQuantity);
        dto.setPrinter(printerQuantity > 0);
        dto.setRfidReader(rfidReaderQuantity > 0);
        dto.setPayment(functional.getPayment().name());

        configuration.getEquipment().keySet().stream()
                .filter(item -> item.getCategory() == EquipmentCategory.BAR_READER)
                .findFirst()
                .ifPresent(scanner -> dto.setBarReader(scanner.getName()));

        dto.setEquipmentSelections(configuration.getEquipment().entrySet().stream()
                .map(entry -> new EquipmentSelectionDTO(
                        entry.getKey().getId(), entry.getValue()))
                .toList());
        configuration.getEquipment().keySet().stream()
                .filter(item -> item.getCategory() == EquipmentCategory.DISPLAY)
                .findFirst()
                .ifPresent(display -> {
                    dto.setDisplay(display.getName());
                    dto.setDisplayWidth(display.getWidth());
                    dto.setDisplayHeight(display.getHeight());
                });
        return dto;
    }

    public static LBC toDomain(LBCDTO dto) {
        if (dto == null) {
            return null;
        }
        TypeLb typeLb = new TypeLb(
                dto.getType(), dto.getDeltaWidth(), dto.getShelfThick(), dto.getServiceZoneWidth());
        return toDomain(dto, typeLb, id -> {
            throw new IllegalStateException(
                    "Для восстановления оборудования требуется загрузчик оборудования");
        }, null);
    }

    public static LBC toDomain(
            LBCDTO dto,
            TypeLb typeLb,
            Function<Long, Equipment> equipmentLookup
    ) {
        return toDomain(dto, typeLb, equipmentResolver, null);
    }

    public static LBC toDomain(
            LBCDTO dto,
            TypeLb typeLb,
            Function<Long, Equipment> equipmentResolver,
            ControlConfiguration fallbackConfiguration
    ) {
        if (dto == null) {
            return null;
        }
        Objects.requireNonNull(typeLb, "Тип LBC не должен быть null");
        Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");
        if (!Objects.equals(dto.getType(), typeLb.getType())) {
            throw new IllegalArgumentException(
                    "Переданный конструктивный тип не соответствует типу DTO LBC");
        }

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

        ControlConfiguration configuration = fallbackConfiguration == null
                ? new ControlConfiguration(functional, Map.of())
                : fallbackConfiguration.copy();
        configuration.setFunctionalConfiguration(functional);
        if (dto.getEquipmentSelections() != null || fallbackConfiguration == null) {
            configuration.setEquipment(equipment);
        }

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
                configuration
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

    public static List<LBCDTO> toDtoList(List<LBC> domains) {
        return domains == null ? List.of() : domains.stream().map(LBCDtoMapper::toDto).toList();
    }
}
