package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
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
    void lbcActsAsControlAndStorageModule() {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.NONE,
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
                new TypeLb("TYPE1", 100, 5, 20),
                10,
                DirectionDoorOpening.LEFT,
                20,
                control
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
