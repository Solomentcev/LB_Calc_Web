package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentRequirementResolverTest {

    private final EquipmentCatalogService catalog = mock(EquipmentCatalogService.class);
    private final EquipmentRequirementResolver resolver = new EquipmentRequirementResolver(catalog);

    @Test
    void resolve_nonePaymentAndNoAccess_shouldNotAddFunctionalEquipment() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.NONE));

        assertTrue(result.getEquipmentConfiguration().getAllItems().isEmpty());
        verify(catalog, never()).findActiveDefault("PAYMENT_TERMINAL");
        verify(catalog, never()).findActiveDefault("CASH_ACCEPTOR");
    }

    @Test
    void resolve_cashless_shouldAddPaymentTerminalAndPrinter() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.CASHLESS));

        assertCategories(result, "PAYMENT_TERMINAL", "PRINTER");
        assertFalse(hasCategory(result, "CASH_ACCEPTOR"));
    }

    @Test
    void resolve_cash_shouldAddCashAcceptorAndPrinterWithoutTerminal() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.CASH));

        assertCategories(result, "CASH_ACCEPTOR", "PRINTER");
        assertFalse(hasCategory(result, "PAYMENT_TERMINAL"));
    }

    @Test
    void resolve_cashAndCards_shouldAddTerminalCashAcceptorAndPrinter() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.CASH_AND_CARDS));

        assertCategories(result, "PAYMENT_TERMINAL", "CASH_ACCEPTOR", "PRINTER");
    }

    @Test
    void resolve_barcode_shouldAddCompatibleReaderAndPrinter() {
        stubDefaults();
        when(catalog.findActiveDefault("BAR_READER"))
                .thenReturn(equipment("BAR_READER", "BAR_READER_1D"));

        ControlConfiguration source = configurationWithAccess(AccessMethod.BARCODE);
        ControlConfiguration result = resolver.resolve(source);

        assertCategories(result, "BAR_READER", "PRINTER");
    }

    @Test
    void resolve_qrCode_shouldRequireTwoDimensionalReaderAndPrinter() {
        stubDefaults();
        when(catalog.findActiveDefault("BAR_READER"))
                .thenReturn(equipment("BAR_READER", "BAR_READER_2D"));

        ControlConfiguration source = configurationWithAccess(AccessMethod.QR_CODE);
        ControlConfiguration result = resolver.resolve(source);

        assertCategories(result, "BAR_READER", "PRINTER");
    }

    @Test
    void resolve_rfid_shouldAddReaderWithoutPrinter() {
        stubDefaults();
        when(catalog.findActiveDefault("RFID_READER"))
                .thenReturn(equipment("RFID_READER", "RFID_READER_STANDARD"));

        ControlConfiguration source = configurationWithAccess(AccessMethod.RFID);
        ControlConfiguration result = resolver.resolve(source);

        assertCategories(result, "RFID_READER");
    }

    @Test
    void resolve_pinWithRfid_shouldAddDefaultDisplayAndPrinter() {
        stubDefaults();
        when(catalog.findActiveDefault("RFID_READER"))
                .thenReturn(equipment("RFID_READER", "RFID_READER_STANDARD", false, false, true, false));

        ControlConfiguration source = configurationWithAccess(AccessMethod.PIN, AccessMethod.RFID);
        ControlConfiguration result = resolver.resolve(source);

        assertCategories(result, "DISPLAY", "RFID_READER", "PRINTER");
    }

    @Test
    void equipmentConfiguration_shouldRejectMultipleDisplays() {
        Equipment firstDisplay = equipment("DISPLAY", "DISPLAY_LC10");
        Equipment secondDisplay = equipment("DISPLAY", "DISPLAY_LC17");

        assertThrows(IllegalArgumentException.class, () ->
                configurationWithAccessAndEquipment(
                        List.of(AccessMethod.PIN, AccessMethod.RFID),
                        List.of(new EquipmentItem(firstDisplay, 1),
                                new EquipmentItem(secondDisplay, 1))));
    }

    @Test
    void equipmentItem_shouldRejectDisplayQuantityGreaterThanOne() {
        Equipment display = equipment("DISPLAY", "DISPLAY_LC10");

        assertThrows(IllegalArgumentException.class,
                () -> new EquipmentItem(display, 2));
    }

    @Test
    void resolve_pinWithoutAdditionalAccessMethod_shouldFail() {
        ControlConfiguration source = configurationWithAccess(AccessMethod.PIN);

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(source));
    }

    @Test
    void resolve_shouldPreserveManuallySelectedCompatiblePaymentTerminal() {
        stubDefaults();
        Equipment terminal = equipment("PAYMENT_TERMINAL", "PAYMENT_TERMINAL_CUSTOM");
        ControlConfiguration source = configurationWithEquipment(
                Payment.CASHLESS,
                List.of(new EquipmentItem(terminal, 1)),
                List.of()
        );

        ControlConfiguration result = resolver.resolve(source);

        assertTrue(result.getEquipmentConfiguration().getAllEquipment().contains(terminal));
        assertCategories(result, "PAYMENT_TERMINAL", "PRINTER");
    }

    @Test
    void resolve_shouldRemoveEquipmentNoLongerRequiredAfterPaymentChange() {
        stubDefaults();
        Equipment terminal = equipment("PAYMENT_TERMINAL", "PAYMENT_TERMINAL_CUSTOM");
        Equipment printer = equipment("PRINTER", "PRINTER_CUSTOM");
        Equipment modem = equipment("MODEM", "MODEM_CUSTOM");
        ControlConfiguration source = configurationWithEquipment(
                Payment.CASH,
                List.of(new EquipmentItem(terminal, 1), new EquipmentItem(printer, 1)),
                List.of(new EquipmentItem(modem, 1))
        );

        ControlConfiguration result = resolver.resolve(source);

        assertCategories(result, "CASH_ACCEPTOR", "PRINTER", "MODEM");
        assertFalse(hasCategory(result, "PAYMENT_TERMINAL"));
        assertTrue(result.getEquipmentConfiguration().getAdditionalEquipment().contains(modem));
    }

    @Test
    void resolve_selectedDisplayWithoutPin_shouldPreserveDisplay() {
        stubDefaults();
        Equipment display = equipment("DISPLAY", "DISPLAY_LC17");
        ControlConfiguration source = configurationWithEquipment(
                Payment.NONE, List.of(new EquipmentItem(display, 1)), List.of());

        ControlConfiguration result = resolver.resolve(source);

        assertCategories(result, "DISPLAY");
        assertTrue(result.getEquipmentConfiguration().getAllEquipment().contains(display));
    }

    @Test
    void resolve_shouldPreserveEquipmentOutsideFunctionalCategories() {
        stubDefaults();
        List<EquipmentItem> additional = List.of(
                new EquipmentItem(equipment("COMPUTER", "COMPUTER_STANDARD"), 1),
                new EquipmentItem(equipment("POWER_SUPPLY", "POWER_SUPPLY_STANDARD"), 1),
                new EquipmentItem(equipment("UPS", "UPS_STANDARD"), 1),
                new EquipmentItem(equipment("LOCK_CONTROLLER", "LOCK_CONTROLLER_STANDARD"), 1)
        );
        ControlConfiguration source = configurationWithEquipment(Payment.NONE, List.of(), additional);

        ControlConfiguration result = resolver.resolve(source);

        assertEquals(
                java.util.Set.of("COMPUTER_STANDARD", "POWER_SUPPLY_STANDARD",
                        "UPS_STANDARD", "LOCK_CONTROLLER_STANDARD"),
                result.getEquipmentConfiguration().getAdditionalItems().stream()
                        .map(item -> item.equipment().getCatalogPositionId())
                        .collect(java.util.stream.Collectors.toSet()));
        assertTrue(result.getEquipmentConfiguration().getRequiredItems().isEmpty());
    }

    private ControlConfiguration configurationWithAccess(AccessMethod... methods) {
        return new ControlConfiguration(Payment.NONE, EnumSet.copyOf(List.of(methods)),
                EnumSet.noneOf(PrintOption.class),
                new EquipmentConfiguration(List.of(), List.of()));
    }

    private ControlConfiguration configurationWithAccessAndEquipment(
            List<AccessMethod> methods, List<EquipmentItem> required) {
        return new ControlConfiguration(Payment.NONE, EnumSet.copyOf(methods),
                EnumSet.noneOf(PrintOption.class),
                EquipmentConfiguration.fromItems(required, List.of()));
    }

    private ControlConfiguration configuration(Payment payment) {
        return configurationWithEquipment(payment, List.of(), List.of());
    }

    private ControlConfiguration configurationWithEquipment(
            Payment payment, List<EquipmentItem> required, List<EquipmentItem> additional) {
        return new ControlConfiguration(payment, EnumSet.noneOf(AccessMethod.class),
                EnumSet.noneOf(PrintOption.class),
                EquipmentConfiguration.fromItems(required, additional));
    }

    private void stubDefaults() {
        Map<String, String> categories = Map.of(
                "PAYMENT_TERMINAL", "PAYMENT_TERMINAL_STANDARD",
                "CASH_ACCEPTOR", "CASH_ACCEPTOR_STANDARD",
                "PRINTER", "PRINTER_STANDARD",
                "DISPLAY", "DISPLAY_LC10"
        );
        categories.forEach((category, id) ->
                when(catalog.findActiveDefault(category)).thenReturn(equipment(category, id)));
    }

    private Equipment equipment(String category, String id) {
        EquipmentCategory equipmentCategory = EquipmentCategory.valueOf(category);
        EquipmentPurpose purpose = switch (equipmentCategory) {
            case DISPLAY -> DisplayPurpose.INCH_10;
            case BAR_READER -> id.endsWith("_2D")
                    ? AccessPurpose.QR_CODE : AccessPurpose.BAR_CODE;
            case RFID_READER -> AccessPurpose.RFID;
            case PAYMENT_TERMINAL -> PaymentPurpose.CARDS;
            case CASH_ACCEPTOR -> PaymentPurpose.CASH;
            case PRINTER -> PrinterPurpose.PRESENT;
            case COMPUTER -> SystemPurpose.COMPUTER;
            case LOCK_CONTROLLER -> SystemPurpose.LOCK_CONTROL;
            case POWER_SUPPLY -> SystemPurpose.POWER_SUPPLY;
            case UPS -> SystemPurpose.BACKUP_POWER;
            case MODEM, ROUTER, ANTENNA, OTHER -> SystemPurpose.OTHER;
        };
        return new Equipment(id, equipmentCategory.getFunctionalType(), purpose,
                equipmentCategory, id, "", 0, 0, 0);
    }

    private boolean hasCategory(ControlConfiguration configuration, String category) {
        return configuration.getEquipmentConfiguration().getAllEquipment().stream()
                .anyMatch(item -> category.equals(item.getCategory()));
    }

    private void assertCategories(ControlConfiguration configuration, String... expected) {
        List<Equipment> equipment = configuration.getEquipmentConfiguration().getAllEquipment();
        assertEquals(expected.length, equipment.size());
        for (String category : expected) {
            assertTrue(equipment.stream().anyMatch(item -> category.equals(item.getCategory())),
                    "Не найдено оборудование категории " + category);
        }
    }
}
