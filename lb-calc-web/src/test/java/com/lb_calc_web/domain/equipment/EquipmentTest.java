package com.lb_calc_web.domain.equipment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EquipmentTest {

    @Test
    void constructor_shouldKeepFunctionalTypePurposeAndCategory() {
        Equipment equipment = new Equipment(
                "BAR_READER_1D",
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER,
                "Сканер штрихкода",
                "",
                0,
                0,
                0
        );

        assertEquals(EquipmentFunctionalType.ACCESS, equipment.getFunctionalType());
        assertEquals(AccessPurpose.BAR_CODE, equipment.getPurpose());
        assertEquals(EquipmentCategory.BAR_READER, equipment.getEquipmentCategory());
    }

    @Test
    void constructor_shouldRejectPurposeFromAnotherFunctionalType() {
        assertThrows(IllegalArgumentException.class, () -> new Equipment(
                "DISPLAY_LC10",
                EquipmentFunctionalType.DISPLAY,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.DISPLAY,
                "LC10",
                "",
                300,
                174,
                0
        ));
    }

    @Test
    void constructor_shouldRejectPurposeIncompatibleWithCategory() {
        assertThrows(IllegalArgumentException.class, () -> new Equipment(
                "RFID_READER_STANDARD",
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.RFID_READER,
                "RFID-считыватель",
                "",
                0,
                0,
                0
        ));
    }

    @Test
    void constructor_shouldRejectFunctionalTypeIncompatibleWithCategory() {
        assertThrows(IllegalArgumentException.class, () -> new Equipment(
                "COMPUTER_STANDARD",
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID,
                EquipmentCategory.COMPUTER,
                "Компьютер",
                "",
                0,
                0,
                0
        ));
    }
}
