package com.lb_calc_web.domain.equipment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentTest {

    @Test
    void constructor_shouldKeepDomainCharacteristics() {
        Equipment equipment = new Equipment(
                1L,
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER,
                "Сканер штрихкода",
                "",
                0,
                0,
                0
        );

        assertEquals(1L, equipment.getId());
        assertEquals(EquipmentFunctionalType.ACCESS, equipment.getFunctionalType());
        assertEquals(AccessPurpose.BAR_CODE, equipment.getPurpose());
        assertEquals(EquipmentCategory.BAR_READER, equipment.getCategory());
    }

    @Test
    void constructor_shouldAllowUnpersistedModelWithoutId() {
        Equipment equipment = new Equipment(
                null,
                EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY,
                "Дисплей 10",
                "",
                255,
                174,
                30
        );

        assertNull(equipment.getId());
    }

    @Test
    void constructor_shouldRejectPurposeFromAnotherFunctionalType() {
        assertThrows(IllegalArgumentException.class, () -> new Equipment(
                1L,
                EquipmentFunctionalType.DISPLAY,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.DISPLAY,
                "Дисплей",
                "",
                300,
                174,
                30
        ));
    }

    @Test
    void constructor_shouldRejectPurposeIncompatibleWithCategory() {
        assertThrows(IllegalArgumentException.class, () -> new Equipment(
                1L,
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
    void transientModels_shouldNotBeEqual() {
        Equipment first = new Equipment(
                null, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID 1", "", 0, 0, 0);
        Equipment second = new Equipment(
                null, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID 1", "", 0, 0, 0);

        assertNotEquals(first, second);
    }
}
