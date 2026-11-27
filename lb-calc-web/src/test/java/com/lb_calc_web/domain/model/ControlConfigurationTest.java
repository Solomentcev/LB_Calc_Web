package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ControlConfigurationTest {

    private FunctionalConfiguration functionalConfiguration(
            DisplayPurpose display,
            Set<AccessPurpose> access,
            PaymentPurpose payment,
            PrinterPurpose printer
    ) {
        return new FunctionalConfiguration(display, access, payment, printer);
    }

    @Test
    void resolver_shouldConvertFunctionalConfigurationToPhysicalEquipmentRequirements() {
        FunctionalConfigurationResolver resolver = new FunctionalConfigurationResolver();
        FunctionalConfiguration configuration = functionalConfiguration(
                DisplayPurpose.NONE,
                Set.of(AccessPurpose.PIN, AccessPurpose.RFID, AccessPurpose.QR_CODE),
                PaymentPurpose.CARDS_AND_CASH,
                PrinterPurpose.PRESENT
        );

        Map<EquipmentFunctionalType, Set<EquipmentPurpose>> requirements =
                resolver.resolveRequiredEquipment(configuration);

        assertEquals(Set.of(DisplayPurpose.INCH_10),
                requirements.get(EquipmentFunctionalType.DISPLAY));
        assertEquals(Set.of(AccessPurpose.RFID, AccessPurpose.QR_CODE),
                requirements.get(EquipmentFunctionalType.ACCESS));
        assertEquals(Set.of(PaymentPurpose.CARDS, PaymentPurpose.CASH),
                requirements.get(EquipmentFunctionalType.PAYMENT));
        assertEquals(Set.of(PrinterPurpose.PRESENT),
                requirements.get(EquipmentFunctionalType.PRINTER));
        assertFalse(requirements.get(EquipmentFunctionalType.ACCESS).contains(AccessPurpose.PIN));
    }

    @Test
    void resolver_shouldNotRequireDisplayWhenPinIsNotSelected() {
        FunctionalConfigurationResolver resolver = new FunctionalConfigurationResolver();
        FunctionalConfiguration configuration = functionalConfiguration(
                DisplayPurpose.NONE, Set.of(AccessPurpose.RFID),
                PaymentPurpose.NONE, PrinterPurpose.ABSENT
        );

        assertFalse(resolver.resolveRequiredEquipment(configuration)
                .containsKey(EquipmentFunctionalType.DISPLAY));
    }

    @Test
    void shouldKeepFunctionalPurposesIndependentFromInstalledEquipment() {
        FunctionalConfiguration functional = functionalConfiguration(
                DisplayPurpose.INCH_10,
                Set.of(AccessPurpose.RFID, AccessPurpose.QR_CODE),
                PaymentPurpose.CARDS,
                PrinterPurpose.PRESENT
        );
        ControlConfiguration configuration = new ControlConfiguration(functional, Map.of());

        assertEquals(functional, configuration.getFunctionalConfiguration());
        assertEquals(Set.of(AccessPurpose.RFID, AccessPurpose.QR_CODE),
                configuration.getFunctionalConfiguration().getAccess());
        assertTrue(configuration.getEquipment().isEmpty());
    }

    @Test
    void shouldNormalizeDisplayWhenPinIsEnabledWithoutDisplay() {
        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.NONE,
                        Set.of(AccessPurpose.PIN),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                Map.of()
        );

        assertEquals(DisplayPurpose.INCH_10,
                configuration.getFunctionalConfiguration().getDisplay());
        assertEquals(Set.of(AccessPurpose.PIN),
                configuration.getFunctionalConfiguration().getAccess());
        assertTrue(configuration.getEquipment().isEmpty());
    }

    @Test
    void shouldStoreEquipmentAndQuantity() {
        Equipment scanner = new Equipment(
                10L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);

        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.NONE,
                        Set.of(AccessPurpose.BAR_CODE),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                Map.of(scanner, 2)
        );

        assertEquals(2, configuration.getQuantity(scanner));
        assertEquals(Map.of(scanner, 2), configuration.getEquipment());
    }

    @Test
    void shouldRejectMoreThanOneDisplay() {
        Equipment first = new Equipment(
                1L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Дисплей 10", "", 0, 0, 0);
        Equipment second = new Equipment(
                2L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_17,
                EquipmentCategory.DISPLAY, "Дисплей 17", "", 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.CUSTOM,
                        Set.of(AccessPurpose.PIN),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                Map.of(first, 1, second, 1)
        ));
    }

    @Test
    void shouldReturnDefensiveEquipmentMap() {
        Equipment scanner = new Equipment(
                10L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);
        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.NONE,
                        Set.of(AccessPurpose.BAR_CODE),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                Map.of(scanner, 1));

        assertThrows(UnsupportedOperationException.class,
                () -> configuration.getEquipment().put(scanner, 5));
    }

    @Test
    void shouldRemoveEquipmentThatNoLongerMatchesFunctionalConfiguration() {
        Equipment scanner = new Equipment(
                10L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);
        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.NONE,
                        Set.of(AccessPurpose.BAR_CODE),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                Map.of(scanner, 2)
        );

        FunctionalConfiguration updated = functionalConfiguration(
                DisplayPurpose.INCH_17,
                Set.of(AccessPurpose.PIN, AccessPurpose.RFID, AccessPurpose.QR_CODE),
                PaymentPurpose.CARDS_AND_CASH,
                PrinterPurpose.OTHER
        );
        configuration.setFunctionalConfiguration(updated);

        assertEquals(updated, configuration.getFunctionalConfiguration());
        assertTrue(configuration.getEquipment().isEmpty());
        assertEquals(
                Map.of(
                        EquipmentFunctionalType.DISPLAY, Set.of(DisplayPurpose.INCH_17),
                        EquipmentFunctionalType.ACCESS, Set.of(AccessPurpose.RFID, AccessPurpose.QR_CODE),
                        EquipmentFunctionalType.PAYMENT, Set.of(PaymentPurpose.CARDS, PaymentPurpose.CASH),
                        EquipmentFunctionalType.PRINTER, Set.of(PrinterPurpose.OTHER)
                ),
                configuration.getRequiredEquipment()
        );
    }

    @Test
    void shouldRejectIncompletePhysicalEquipmentConfiguration() {
        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.INCH_10,
                        Set.of(AccessPurpose.RFID),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                Map.of()
        );

        assertThrows(IllegalStateException.class, configuration::validateEquipmentRequirements);
    }

    @Test
    void combinedPaymentEquipmentShouldSatisfyCardAndCashRequirements() {
        Equipment combinedPayment = new Equipment(
                41L, EquipmentFunctionalType.PAYMENT, PaymentPurpose.CARDS_AND_CASH,
                EquipmentCategory.PAYMENT_TERMINAL, "Комбинированный терминал", "", 0, 0, 0);
        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration(
                        DisplayPurpose.NONE, Set.of(),
                        PaymentPurpose.CARDS_AND_CASH, PrinterPurpose.ABSENT
                ),
                Map.of(combinedPayment, 1)
        );

        assertDoesNotThrow(configuration::validateEquipmentRequirements);
    }

    @Test
    void equipmentChanges_shouldResolveOnlyAffectedFunctionalAreas() {
        FunctionalConfiguration initial = functionalConfiguration(
                DisplayPurpose.CUSTOM, Set.of(AccessPurpose.PIN),
                PaymentPurpose.CASH, PrinterPurpose.OTHER);
        ControlConfiguration configuration = new ControlConfiguration(initial, Map.of());

        Equipment scanner = new Equipment(
                21L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);
        Equipment terminal = new Equipment(
                22L, EquipmentFunctionalType.PAYMENT, PaymentPurpose.CARDS,
                EquipmentCategory.PAYMENT_TERMINAL, "Терминал", "", 0, 0, 0);

        configuration.addEquipment(scanner, 1);
        configuration.addEquipment(terminal, 1);
        FunctionalConfiguration afterAdd = configuration.getFunctionalConfiguration();

        assertEquals(DisplayPurpose.CUSTOM, afterAdd.getDisplay());
        assertEquals(Set.of(AccessPurpose.PIN, AccessPurpose.BAR_CODE), afterAdd.getAccess());
        assertEquals(PaymentPurpose.CARDS, afterAdd.getPayment());
        assertEquals(PrinterPurpose.OTHER, afterAdd.getPrinter());

        configuration.removeEquipment(scanner);
        configuration.removeEquipment(terminal);
        FunctionalConfiguration afterRemove = configuration.getFunctionalConfiguration();

        assertEquals(DisplayPurpose.CUSTOM, afterRemove.getDisplay());
        assertEquals(Set.of(AccessPurpose.PIN), afterRemove.getAccess());
        assertEquals(PaymentPurpose.NONE, afterRemove.getPayment());
        assertEquals(PrinterPurpose.OTHER, afterRemove.getPrinter());
    }

    @Test
    void failedEquipmentChange_shouldNotPartiallyUpdateConfiguration() {
        Equipment first = new Equipment(
                31L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Дисплей 10", "", 0, 0, 0);
        Equipment second = new Equipment(
                32L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_17,
                EquipmentCategory.DISPLAY, "Дисплей 17", "", 0, 0, 0);
        FunctionalConfiguration initial = functionalConfiguration(
                DisplayPurpose.NONE, Set.of(AccessPurpose.RFID),
                PaymentPurpose.CASH, PrinterPurpose.ABSENT);
        ControlConfiguration configuration = new ControlConfiguration(initial, Map.of(first, 1));

        assertThrows(IllegalArgumentException.class, () -> configuration.addEquipment(second, 1));
        assertEquals(Map.of(first, 1), configuration.getEquipment());
        assertEquals(initial, configuration.getFunctionalConfiguration());
    }

}
