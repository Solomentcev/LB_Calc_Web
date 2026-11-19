package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;

public final class LCDtoMapper {

    private LCDtoMapper() {
    }

    public static LCDTO toDto(LC domain) {
        if (domain == null) {
            return null;
        }

        LCDTO dto = new LCDTO();

        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());

        dto.setHeight(domain.getHeight());
        dto.setWidth(domain.getWidth());
        dto.setDepth(domain.getDepth());

        dto.setUpperFrame(domain.getUpperFrame());
        dto.setBottomFrame(domain.getBottomFrame());

        dto.setColorBody(domain.getColorBody().name());
        dto.setColorDoor(domain.getColorDoor().name());

        ControlConfiguration configuration =
                domain.getControlConfiguration();

        dto.setPayment(
                configuration.getPayment().name()
        );

        dto.setAccessMethods(
                configuration.getAccessMethods()
                        .stream()
                        .map(Enum::name)
                        .toList()
        );

        dto.setPrintOptions(
                configuration.getPrintOptions()
                        .stream()
                        .map(Enum::name)
                        .toList()
        );

        List<Equipment> equipment =
                configuration.getEquipmentConfiguration()
                        .getAllEquipment();

        Equipment display = equipment.stream()
                .filter(Equipment::isDisplay)
                .filter(value -> !Display.NONE_ID.equals(value.getCatalogPositionId()))
                .findFirst()
                .orElse(null);

        Equipment barReader = equipment.stream()
                .filter(Equipment::isBarReader)
                .filter(value -> !BarReader.NONE_ID.equals(value.getCatalogPositionId()))
                .findFirst()
                .orElse(null);

        dto.setDisplay(
                display != null
                        ? display.getName()
                        : Display.NONE.getName()
        );
        dto.setDisplayWidth(display == null ? 0 : display.getDisplayWidth());
        dto.setDisplayHeight(display == null ? 0 : display.getDisplayHeight());

        dto.setBarReader(
                barReader != null
                        ? barReader.getName()
                        : BarReader.NONE.getName()
        );

        dto.setPrinter(
                equipment.stream()
                        .anyMatch(Equipment::isPrinter)
        );

        dto.setRfidReader(
                equipment.stream().anyMatch(Equipment::isRfidReader)
        );

        EquipmentConfiguration equipmentConfiguration =
                configuration.getEquipmentConfiguration();
        dto.setEquipmentSelections(equipmentConfiguration.getAllItems().stream()
                .map(item -> new EquipmentSelectionDTO(
                        item.equipment().getCatalogPositionId(), item.quantity()))
                .toList());
        dto.setDisplayQuantity(display == null ? 0
                : equipmentConfiguration.getQuantity(display));
        dto.setBarReaderQuantity(barReader == null ? 0
                : equipmentConfiguration.getQuantity(barReader));
        dto.setPrinterQuantity(equipment.stream()
                .filter(Equipment::isPrinter).findFirst()
                .map(equipmentConfiguration::getQuantity).orElse(0));
        dto.setRfidReaderQuantity(equipment.stream()
                .filter(Equipment::isRfidReader).findFirst()
                .map(equipmentConfiguration::getQuantity).orElse(0));

        return dto;
    }

    public static LC toDomain(LCDTO dto) {
        return toDomain(dto, EquipmentCatalog::resolve);
    }

    public static LC toDomain(LCDTO dto, Function<String, Equipment> equipmentResolver) {
        if (dto == null) {
            return null;
        }
        java.util.Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");

        EquipmentConfiguration equipmentConfiguration =
                dto.getEquipmentSelections() != null
                        ? ControlEquipmentResolver.resolve(
                                dto.getEquipmentSelections(), equipmentResolver)
                        : ControlEquipmentResolver.resolve(
                                dto.getDisplay(), dto.getDisplayQuantity(),
                                dto.getBarReader(), dto.getBarReaderQuantity(),
                                dto.isPrinter(), dto.getPrinterQuantity(),
                                dto.isRfidReader(), dto.getRfidReaderQuantity(),
                                "LC", equipmentResolver);

        ControlConfiguration controlConfiguration =
                new ControlConfiguration(
                        Payment.valueOf(dto.getPayment()),
                        toAccessMethods(dto.getAccessMethods()),
                        toPrintOptions(dto.getPrintOptions()),
                        equipmentConfiguration
                );

        return new LC(
                dto.getHeight(),
                dto.getWidth(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor()),
                controlConfiguration
        );
    }

    private static Set<AccessMethod> toAccessMethods(
            List<String> values
    ) {
        EnumSet<AccessMethod> result =
                EnumSet.noneOf(AccessMethod.class);

        if (values == null) {
            return result;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(AccessMethod.valueOf(value));
            }
        }

        return result;
    }

    private static Set<PrintOption> toPrintOptions(
            List<String> values
    ) {
        EnumSet<PrintOption> result =
                EnumSet.noneOf(PrintOption.class);

        if (values == null) {
            return result;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(PrintOption.valueOf(value));
            }
        }

        return result;
    }

    public static List<LCDTO> toDtoList(
            List<LC> domains
    ) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(LCDtoMapper::toDto)
                .toList();
    }

    public static List<LC> toDomainList(
            List<LCDTO> dtos
    ) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(LCDtoMapper::toDomain)
                .toList();
    }
}