package com.lb_calc_web.domain.model;

import com.lb_calc_web.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ALSDomainTest {

    @Test
    void addStorageModule_shouldSupportMoreThanThreeModulesAndKeepCenterBalanced() {
        ALS als = TestDataFactory.validALS(1L);

        als.addStorageModule(TestDataFactory.validLB(301L));
        als.addStorageModule(TestDataFactory.validLB(302L));
        als.addStorageModule(TestDataFactory.validLB(303L));

        assertEquals(6, als.getModules().size());
        assertEquals("CENTER", als.getPositionControlModule().name());
        assertEquals(
                List.of("LB", "LB", "LC", "LB", "LB", "LB"),
                als.getModules().stream()
                        .map(module -> module.getClass().getSimpleName())
                        .toList()
        );
    }


    @Test
    void replaceLBCWithLC_shouldRemoveEmbeddedStorageAndPreserveControlConfiguration() {
        LB left = TestDataFactory.validLB(101L);
        LBC lbc = TestDataFactory.validLBC(102L);
        LB right = TestDataFactory.validLB(103L);
        ALS als = new ALS(
                1940, 500, 50, 50,
                lbc.getColorBody(), lbc.getColorDoor(),
                List.of(left, lbc, right)
        );

        LC replacementLC = new LC(
                lbc.getHeight(), lbc.getWidth(), lbc.getDepth(),
                lbc.getUpperFrame(), lbc.getBottomFrame(),
                lbc.getColorBody(), lbc.getColorDoor(),
                lbc.getControlConfiguration(),
                TestDataFactory.createSizeBounds()
        );

        als.replaceLBCWithLC(replacementLC);

        assertEquals(
                List.of("LB", "LC", "LB"),
                als.getModules().stream()
                        .map(module -> module.getClass().getSimpleName())
                        .toList()
        );
        assertEquals("CENTER", als.getPositionControlModule().name());
        assertEquals(6, als.getCountCells());
        assertEquals(
                lbc.getControlConfiguration(),
                als.getLC().getControlConfiguration()
        );
    }

    @Test
    void replaceLBCWithLC_shouldRejectALSWithoutOtherStorageModulesAtomically() {
        LBC lbc = TestDataFactory.validLBC(201L);
        ALS als = new ALS(
                1940, 500, 50, 50,
                lbc.getColorBody(), lbc.getColorDoor(),
                List.of(lbc)
        );

        LC replacement = new LC(
                lbc.getHeight(), lbc.getWidth(), lbc.getDepth(),
                lbc.getUpperFrame(), lbc.getBottomFrame(),
                lbc.getColorBody(), lbc.getColorDoor(),
                lbc.getControlConfiguration(),
                TestDataFactory.createSizeBounds()
        );

        assertThrows(
                IllegalStateException.class,
                () -> als.replaceLBCWithLC(replacement)
        );
        assertNotNull(als.getLBC());
        assertEquals(3, als.getCountCells());
    }

    @Test
    void copy_shouldPreserveEqualityAndHashCodeContract() {
        ALS original = TestDataFactory.validALS(1L);
        ALS copy = original.copy();

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    @Test
    void constructor_shouldRejectUnbalancedCenterComposition() {
        assertThrows(
                IllegalStateException.class,
                () -> new ALS(
                        1940,
                        500,
                        50,
                        50,
                        TestDataFactory.validALS(1L).getColorBody(),
                        TestDataFactory.validALS(1L).getColorDoor(),
                        List.of(
                                TestDataFactory.validLB(1L),
                                TestDataFactory.validLB(2L),
                                TestDataFactory.validLB(3L),
                                TestDataFactory.validLC(4L),
                                TestDataFactory.validLB(5L)
                        )
                )
        );
    }

    @Test
    void constructor_shouldAllowLbcWithMultipleStorageModules() {
        ALS als = new ALS(
                1940,
                500,
                50,
                50,
                TestDataFactory.validALS(1L).getColorBody(),
                TestDataFactory.validALS(1L).getColorDoor(),
                List.of(
                        TestDataFactory.validLB(1L),
                        TestDataFactory.validLB(2L),
                        TestDataFactory.validLBC(3L),
                        TestDataFactory.validLB(4L),
                        TestDataFactory.validLB(5L)
                )
        );

        assertEquals("CENTER", als.getPositionControlModule().name());
        assertEquals(5, als.getModules().size());
    }
    @Test
    void equipmentAndFunctionalConfigurationChanges_shouldStaySynchronized() {
        ALS als = TestDataFactory.validALS(71L);
        var scanner = new com.lb_calc_web.domain.equipment.Equipment(
                71L,
                com.lb_calc_web.domain.equipment.EquipmentFunctionalType.ACCESS,
                com.lb_calc_web.domain.equipment.AccessPurpose.BAR_CODE,
                com.lb_calc_web.domain.equipment.EquipmentCategory.BAR_READER,
                "Сканер", "", 0, 0, 0);

        als.addEquipment(scanner, 1);
        assertEquals(java.util.Set.of(com.lb_calc_web.domain.equipment.AccessPurpose.BAR_CODE),
                als.getFunctionalConfiguration().getAccess());
        assertEquals(als.getFunctionalConfiguration(),
                als.getControlModule().getControlConfiguration().getFunctionalConfiguration());

        FunctionalConfiguration manual = new FunctionalConfiguration(
                com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_17,
                java.util.Set.of(com.lb_calc_web.domain.equipment.AccessPurpose.RFID),
                com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS,
                com.lb_calc_web.domain.equipment.PrinterPurpose.PRESENT);
        als.setFunctionalConfiguration(manual);

        assertEquals(manual, als.getControlModule()
                .getControlConfiguration().getFunctionalConfiguration());
        assertEquals(1, als.getControlModule().getEquipment().size());

        als.removeEquipment(scanner);
        assertTrue(als.getFunctionalConfiguration().getAccess().isEmpty());
        assertEquals(com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_17,
                als.getFunctionalConfiguration().getDisplay());
        assertEquals(com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS,
                als.getFunctionalConfiguration().getPayment());
        assertEquals(com.lb_calc_web.domain.equipment.PrinterPurpose.PRESENT,
                als.getFunctionalConfiguration().getPrinter());
    }

    @Test
    void setControlConfiguration_shouldUpdateAlsFunctionalConfiguration() {
        ALS als = TestDataFactory.validALS(72L);
        FunctionalConfiguration functional = new FunctionalConfiguration(
                com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_19,
                java.util.Set.of(com.lb_calc_web.domain.equipment.AccessPurpose.RFID),
                com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS_AND_CASH,
                com.lb_calc_web.domain.equipment.PrinterPurpose.PRESENT
        );
        var scanner = new com.lb_calc_web.domain.equipment.Equipment(
                72L,
                com.lb_calc_web.domain.equipment.EquipmentFunctionalType.ACCESS,
                com.lb_calc_web.domain.equipment.AccessPurpose.RFID,
                com.lb_calc_web.domain.equipment.EquipmentCategory.RFID_READER,
                "RFID-считыватель", "", 0, 0, 0
        );
        ControlConfiguration configuration =
                new ControlConfiguration(functional, java.util.Map.of(scanner, 1));

        als.setControlConfiguration(configuration);

        assertEquals(functional, als.getFunctionalConfiguration());
        assertEquals(functional, als.getControlModule()
                .getControlConfiguration().getFunctionalConfiguration());
        assertEquals(java.util.Map.of(scanner, 1), als.getControlModule().getEquipment());
    }

}
