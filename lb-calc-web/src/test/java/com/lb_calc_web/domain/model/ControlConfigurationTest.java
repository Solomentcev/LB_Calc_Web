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
    void shouldPreserveEquipmentWhenFunctionalConfigurationChanges() {
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
        assertEquals(Map.of(scanner, 2), configuration.getEquipment());
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
        assertEquals(Set.of(AccessPurpose.BAR_CODE), afterAdd.getAccess());
        assertEquals(PaymentPurpose.CARDS, afterAdd.getPayment());
        assertEquals(PrinterPurpose.OTHER, afterAdd.getPrinter());

        configuration.removeEquipment(scanner);
        configuration.removeEquipment(terminal);
        FunctionalConfiguration afterRemove = configuration.getFunctionalConfiguration();

        assertEquals(DisplayPurpose.CUSTOM, afterRemove.getDisplay());
        assertTrue(afterRemove.getAccess().isEmpty());
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
