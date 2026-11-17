package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class LBCDtoMapper {

    private LBCDtoMapper() {
    }

    public static LBCDTO toDto(LBC domain) {
        if (domain == null) {
            return null;
        }

        LBCDTO dto = new LBCDTO();

        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());

        dto.setType(domain.getTypeLb().getType());
        dto.setDeltaWidth(domain.getTypeLb().getDeltaWidth());
        dto.setShelfThick(domain.getTypeLb().getShelfThick());
        dto.setServiceZoneWidth(
                domain.getTypeLb().getServiceZoneWidth()
        );

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

        dto.setDirectionDoorOpening(
                domain.getDirectionDoorOpening().name()
        );

        dto.setColorBody(
                domain.getColorBody().name()
        );

        dto.setColorDoor(
                domain.getColorDoor().name()
        );

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

        Display display = equipment.stream()
                .filter(Display.class::isInstance)
                .map(Display.class::cast)
                .filter(value -> value != Display.NONE)
                .findFirst()
                .orElse(null);

        BarReader barReader = equipment.stream()
                .filter(BarReader.class::isInstance)
                .map(BarReader.class::cast)
                .filter(value -> value != BarReader.NONE)
                .findFirst()
                .orElse(null);

        dto.setDisplay(
                display != null
                        ? display.getName()
                        : Display.NONE.getName()
        );

        dto.setBarReader(
                barReader != null
                        ? barReader.getName()
                        : BarReader.NONE.getName()
        );

        dto.setPrinter(
                equipment.stream()
                        .anyMatch(Printer.class::isInstance)
        );

        dto.setRfidReader(
                equipment.stream().anyMatch(RfidReader.class::isInstance)
        );

        EquipmentConfiguration equipmentConfiguration =
                configuration.getEquipmentConfiguration();
        dto.setDisplayQuantity(display == null ? 0
                : equipmentConfiguration.getQuantity(display));
        dto.setBarReaderQuantity(barReader == null ? 0
                : equipmentConfiguration.getQuantity(barReader));
        dto.setPrinterQuantity(equipment.stream()
                .filter(Printer.class::isInstance).findFirst()
                .map(equipmentConfiguration::getQuantity).orElse(0));
        dto.setRfidReaderQuantity(equipment.stream()
                .filter(RfidReader.class::isInstance).findFirst()
                .map(equipmentConfiguration::getQuantity).orElse(0));

        return dto;
    }

    public static LBC toDomain(LBCDTO dto) {
        if (dto == null) {
            return null;
        }

        TypeLb typeLb = new TypeLb(
                dto.getType(),
                dto.getDeltaWidth(),
                dto.getShelfThick(),
                dto.getServiceZoneWidth()
        );

        List<Equipment> equipment = new ArrayList<>();

        int displayQuantity = dto.getDisplayQuantity() == null
                ? (dto.getDisplay() == null || dto.getDisplay().isBlank()
                    || Display.NONE.getName().equals(dto.getDisplay()) ? 0 : 1)
                : dto.getDisplayQuantity();
        int barReaderQuantity = dto.getBarReaderQuantity() == null
                ? (dto.getBarReader() == null || dto.getBarReader().isBlank()
                    || BarReader.NONE.getName().equals(dto.getBarReader()) ? 0 : 1)
                : dto.getBarReaderQuantity();
        int printerQuantity = dto.getPrinterQuantity() == null
                ? (dto.isPrinter() ? 1 : 0) : dto.getPrinterQuantity();
        int rfidReaderQuantity = dto.getRfidReaderQuantity() == null
                ? (dto.isRfidReader() ? 1 : 0) : dto.getRfidReaderQuantity();

        requireNonNegativeQuantity(displayQuantity);
        requireNonNegativeQuantity(barReaderQuantity);
        requireNonNegativeQuantity(printerQuantity);
        requireNonNegativeQuantity(rfidReaderQuantity);

        if (displayQuantity > 0) {
            addDisplay(dto.getDisplay(), equipment);
            repeatEquipment(equipment, Display.class, displayQuantity);
        }
        if (barReaderQuantity > 0) {
            addBarReader(dto.getBarReader(), equipment);
            repeatEquipment(equipment, BarReader.class, barReaderQuantity);
        }
        addEquipmentCopies(equipment, new Printer(), printerQuantity);
        addEquipmentCopies(equipment, new RfidReader(), rfidReaderQuantity);

        EquipmentConfiguration equipmentConfiguration =
                new EquipmentConfiguration(
                        equipment,
                        List.of()
                );

        ControlConfiguration controlConfiguration =
                new ControlConfiguration(
                        Payment.valueOf(dto.getPayment()),
                        toAccessMethods(dto.getAccessMethods()),
                        toPrintOptions(dto.getPrintOptions()),
                        equipmentConfiguration
                );

        return new LBC(
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
                dto.getDoorThickness(),
                controlConfiguration
        );
    }

    public static List<LBCDTO> toDtoList(
            List<LBC> domains
    ) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(LBCDtoMapper::toDto)
                .toList();
    }

    public static List<LBC> toDomainList(
            List<LBCDTO> dtos
    ) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(LBCDtoMapper::toDomain)
                .toList();
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
                result.add(
                        AccessMethod.valueOf(value)
                );
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
                result.add(
                        PrintOption.valueOf(value)
                );
            }
        }

        return result;
    }

    private static void requireNonNegativeQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Количество оборудования не может быть отрицательным");
        }
    }

    private static void repeatEquipment(
            List<Equipment> equipment,
            Class<? extends Equipment> type,
            int quantity
    ) {
        Equipment position = equipment.stream()
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Не удалось определить позицию оборудования"));
        for (int i = 1; i < quantity; i++) {
            equipment.add(position);
        }
    }

    private static void addEquipmentCopies(
            List<Equipment> equipment,
            Equipment position,
            int quantity
    ) {
        for (int i = 0; i < quantity; i++) {
            equipment.add(position);
        }
    }

    private static void addDisplay(
            String name,
            List<Equipment> equipment
    ) {
        if (name == null || name.isBlank()) {
            return;
        }

        if (name.equals(Display.NONE.getName())) {
            return;
        }

        for (Display display : List.of(
                Display.LC10,
                Display.LC17,
                Display.LC19
        )) {
            if (display.getName().equals(name)) {
                equipment.add(display);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Неизвестный дисплей LBC: " + name
        );
    }

    private static void addBarReader(
            String name,
            List<Equipment> equipment
    ) {
        if (name == null || name.isBlank()) {
            return;
        }

        if (name.equals(BarReader.NONE.getName())) {
            return;
        }

        for (BarReader reader : List.of(
                BarReader.READER_1D,
                BarReader.READER_2D
        )) {
            if (reader.getName().equals(name)) {
                equipment.add(reader);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Неизвестный сканер штрихкода LBC: " + name
        );
    }
}