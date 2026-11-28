package com.lb_calc_web.domain.model;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LBCDomainTest {

    @Test
    void technicalCellWithoutPhysicalDisplayUsesMinimumDimensions() {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_10, Set.of(), PaymentPurpose.NONE, PrinterPurpose.ABSENT);
        LBC module = new LBC(1940, 500, 600, 100, 100,
                Colors.Blue, Colors.White, TypeLb.TYPE1,
                10, DirectionDoorOpening.LEFT, 20,
                new ControlConfiguration(functional, Map.of()),
                TestDataFactory.createSizeBounds());

        assertEquals(200, module.getTechnicalCellWidth());
        assertEquals(200, module.getTechnicalCellHeight());
        assertEquals(-1, module.getDisplayTop());
    }

    @Test
    void technicalCellUsesPhysicalDisplayDimensionsAndClearance() {
        Equipment display = new Equipment(1L, EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10, EquipmentCategory.DISPLAY,
                "Дисплей", "", 300, 180, 30);
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_10, Set.of(), PaymentPurpose.NONE, PrinterPurpose.ABSENT);
        LBC module = new LBC(1940, 500, 600, 100, 100,
                Colors.Blue, Colors.White, TypeLb.TYPE1,
                10, DirectionDoorOpening.LEFT, 20,
                new ControlConfiguration(functional, Map.of(display, 1)),
                TestDataFactory.createSizeBounds());

        assertEquals(340, module.getTechnicalCellWidth());
        assertEquals(220, module.getTechnicalCellHeight());
        assertEquals(module.getTechnicalCellTop() + 20, module.getDisplayTop());
    }

    @Test
    void displaySetsMinimumStorageCellWidthForTypeThree() {
        Equipment display = new Equipment(2L, EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10, EquipmentCategory.DISPLAY,
                "Дисплей", "", 300, 180, 30);
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_10, Set.of(), PaymentPurpose.NONE, PrinterPurpose.ABSENT);
        LBC module = new LBC(1940, 200, 600, 100, 100,
                Colors.Blue, Colors.White, TypeLb.TYPE3,
                10, DirectionDoorOpening.LEFT, 20,
                new ControlConfiguration(functional, Map.of(display, 1)),
                TestDataFactory.createSizeBounds());

        assertEquals(340, module.getTechnicalCellWidth());
        assertEquals(500, module.getWidth());
        assertEquals(340, module.getCellWidth());
    }

    @Test
    void storageCellWidthForLbcIsAtLeastTwoHundredForEveryType() {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.NONE, Set.of(), PaymentPurpose.NONE, PrinterPurpose.ABSENT);
        for (TypeLb type : TypeLb.values()) {
            LBC module = new LBC(1940, 200, 600, 100, 100,
                    Colors.Blue, Colors.White, type,
                    10, DirectionDoorOpening.LEFT, 20,
                    new ControlConfiguration(functional, Map.of()),
                    TestDataFactory.createSizeBounds());

            assertEquals(200, module.getCellWidth(), type.name());
            assertEquals(200 + type.getDeltaWidth(), module.getWidth(), type.name());
        }
    }

    @Test
    void lbcActsAsControlAndStorageModule() {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_10,
                Set.of(AccessPurpose.PIN),
                PaymentPurpose.NONE,
                PrinterPurpose.ABSENT
        );
        ControlConfiguration control = new ControlConfiguration(functional, Map.of());

        LBC lbc = new LBC(
                1940,
                500,
                600,
                100,
                100,
                Colors.Blue,
                Colors.White,
                TypeLb.TYPE1,
                10,
                DirectionDoorOpening.LEFT,
                20,
                control,
                TestDataFactory.createSizeBounds()
        );

        ALS als = new ALS(
                1940,
                600,
                100,
                100,
                Colors.Blue,
                Colors.White,
                List.of(lbc)
        );

        assertTrue(lbc instanceof ControlModule);
        assertTrue(lbc instanceof StorageModule);
        assertEquals(functional, als.getFunctionalConfiguration());
        assertEquals(
                functional,
                als.getLBC().getControlConfiguration().getFunctionalConfiguration()
        );

        FunctionalConfiguration updated = new FunctionalConfiguration(
                DisplayPurpose.INCH_10,
                Set.of(AccessPurpose.RFID, AccessPurpose.QR_CODE),
                PaymentPurpose.CARDS,
                PrinterPurpose.PRESENT
        );
        ALS copy = als.copy();
        copy.setFunctionalConfiguration(updated);

        assertEquals(functional, als.getFunctionalConfiguration());
        assertEquals(updated, copy.getFunctionalConfiguration());
        assertEquals(
                updated,
                copy.getLBC().getControlConfiguration().getFunctionalConfiguration()
        );

        als.setFunctionalConfiguration(updated);
        assertEquals(updated, als.getFunctionalConfiguration());
        assertEquals(
                updated,
                als.getLBC().getControlConfiguration().getFunctionalConfiguration()
        );

        assertEquals(1, als.getStorageModules().size());
        assertEquals(10, als.getCountCells());
        assertEquals(500, als.getWidth());
        assertEquals(149, lbc.getCellHeight());
        assertEquals(400, lbc.getCellWidth());
        assertEquals(580, lbc.getCellDepth());
        assertEquals(200, lbc.getTechnicalCellWidth());
        assertEquals(200, lbc.getTechnicalCellHeight());
        assertEquals(5, lbc.getStorageCellsAbove());
        assertEquals(5, lbc.getStorageCellsBelow());
        assertEquals(870, lbc.getTechnicalCellTop());
        assertEquals(
                com.lb_calc_web.domain.attributes.PositionControlModule.CENTER,
                als.getPositionControlModule()
        );
        assertEquals(DirectionDoorOpening.LEFT, lbc.getDirectionDoorOpening());
        assertTrue(lbc.getEquipment().isEmpty());
    }
}
