package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.equipment.CatalogEquipment;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentItem;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
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

        assertEquals(additional, result.getEquipmentConfiguration().getAdditionalItems());
        assertTrue(result.getEquipmentConfiguration().getRequiredItems().isEmpty());
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
        return new CatalogEquipment(id, category, id, "", 0, 0, 0,
                0, 0, 0, false, false, false, "PRINTER".equals(category));
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
