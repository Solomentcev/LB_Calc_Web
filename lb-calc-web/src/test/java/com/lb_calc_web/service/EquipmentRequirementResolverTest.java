package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.CatalogEquipment;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentRequirementResolverTest {

    private final EquipmentCatalogService catalog = mock(EquipmentCatalogService.class);
    private final EquipmentRequirementResolver resolver = new EquipmentRequirementResolver(catalog);

    @Test
    void resolve_nonePayment_shouldAddBaseEquipmentWithoutPaymentHardware() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.NONE));

        assertCategories(result.getEquipmentConfiguration().getAllEquipment(),
                "COMPUTER", "LOCK_CONTROLLER", "POWER_SUPPLY", "UPS");
        assertEquals(4, result.getEquipmentConfiguration().getAllItems().stream()
                .filter(item -> Set.of("COMPUTER", "LOCK_CONTROLLER", "POWER_SUPPLY", "UPS")
                        .contains(item.equipment().getCategory()))
                .filter(item -> item.quantity() == 1)
                .count());
        verify(catalog, never()).findActiveDefault("PAYMENT_TERMINAL");
        verify(catalog, never()).findActiveDefault("CASH_ACCEPTOR");
    }

    @Test
    void resolve_cashless_shouldAddPaymentTerminalAndPrinter() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.CASHLESS));

        assertCategories(result.getEquipmentConfiguration().getAllEquipment(),
                "COMPUTER", "LOCK_CONTROLLER", "POWER_SUPPLY", "UPS",
                "PAYMENT_TERMINAL", "PRINTER");
        assertFalse(hasCategory(result, "CASH_ACCEPTOR"));
    }

    @Test
    void resolve_cashAndCards_shouldAddTerminalCashAcceptorAndPrinter() {
        stubDefaults();

        ControlConfiguration result = resolver.resolve(configuration(Payment.CASH_AND_CARDS));

        assertCategories(result.getEquipmentConfiguration().getAllEquipment(),
                "COMPUTER", "LOCK_CONTROLLER", "POWER_SUPPLY", "UPS",
                "PAYMENT_TERMINAL", "CASH_ACCEPTOR", "PRINTER");
    }

    @Test
    void resolve_shouldPreserveAdditionalEquipment() {
        stubDefaults();
        Equipment modem = equipment("MODEM", "MODEM_OPTIONAL");
        ControlConfiguration source = new ControlConfiguration(
                Payment.NONE, EnumSet.noneOf(AccessMethod.class), EnumSet.noneOf(
                com.lb_calc_web.domain.attributes.PrintOption.class),
                EquipmentConfiguration.fromItems(List.of(), List.of(
                        new com.lb_calc_web.domain.equipment.EquipmentItem(modem, 1)))
        );

        ControlConfiguration result = resolver.resolve(source);

        assertEquals(1, result.getEquipmentConfiguration().getAdditionalItems().size());
        assertEquals("MODEM_OPTIONAL", result.getEquipmentConfiguration()
                .getAdditionalItems().get(0).equipment().getCatalogPositionId());
    }

    private ControlConfiguration configuration(Payment payment) {
        return new ControlConfiguration(payment, EnumSet.noneOf(AccessMethod.class),
                EnumSet.noneOf(com.lb_calc_web.domain.attributes.PrintOption.class),
                new EquipmentConfiguration(List.of(), List.of()));
    }

    private void stubDefaults() {
        Map<String, String> categories = Map.of(
                "COMPUTER", "COMPUTER_STANDARD",
                "LOCK_CONTROLLER", "LOCK_CONTROLLER_STANDARD",
                "POWER_SUPPLY", "POWER_SUPPLY_STANDARD",
                "UPS", "UPS_STANDARD",
                "PAYMENT_TERMINAL", "PAYMENT_TERMINAL_STANDARD",
                "CASH_ACCEPTOR", "CASH_ACCEPTOR_STANDARD",
                "PRINTER", "PRINTER_STANDARD"
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

    private void assertCategories(List<Equipment> equipment, String... expected) {
        assertEquals(expected.length, equipment.size());
        for (String category : expected) {
            assertTrue(equipment.stream().anyMatch(item -> category.equals(item.getCategory())),
                    "Не найдено оборудование категории " + category);
        }
    }
}
