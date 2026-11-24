package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ControlConfigurationTest {

    @Test
    void shouldKeepFunctionalPurposesIndependentFromInstalledEquipment() {
        ControlConfiguration configuration = new ControlConfiguration(
                DisplayPurpose.INCH_10,
                AccessPurpose.RFID,
                PaymentPurpose.CARDS,
                PrinterPurpose.PRESENT,
                Map.of()
        );

        assertEquals(DisplayPurpose.INCH_10, configuration.getDisplay());
        assertEquals(AccessPurpose.RFID, configuration.getAccess());
        assertEquals(PaymentPurpose.CARDS, configuration.getPayment());
        assertEquals(PrinterPurpose.PRESENT, configuration.getPrinter());
        assertTrue(configuration.getEquipment().isEmpty());
    }

    @Test
    void shouldStoreEquipmentAndQuantity() {
        Equipment scanner = new Equipment(
                10L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);

        ControlConfiguration configuration = new ControlConfiguration(
                DisplayPurpose.NONE,
                AccessPurpose.BAR_CODE,
                PaymentPurpose.NONE,
                PrinterPurpose.ABSENT,
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
                DisplayPurpose.CUSTOM,
                AccessPurpose.PIN,
                PaymentPurpose.NONE,
                PrinterPurpose.ABSENT,
                Map.of(first, 1, second, 1)
        ));
    }

    @Test
    void shouldReturnDefensiveEquipmentMap() {
        Equipment scanner = new Equipment(
                10L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);
        ControlConfiguration configuration = new ControlConfiguration(
                DisplayPurpose.NONE, AccessPurpose.BAR_CODE, PaymentPurpose.NONE,
                PrinterPurpose.ABSENT, Map.of(scanner, 1));

        assertThrows(UnsupportedOperationException.class,
                () -> configuration.getEquipment().put(scanner, 5));
    }
}
