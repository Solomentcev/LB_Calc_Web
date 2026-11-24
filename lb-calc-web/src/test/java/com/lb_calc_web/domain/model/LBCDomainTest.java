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
        assertEquals(1, als.getStorageModules().size());
        assertEquals(10, als.getCountCells());
        assertEquals(500, als.getWidth());
        assertEquals(169, lbc.getCellHeight());
        assertEquals(380, lbc.getCellWidth());
        assertEquals(580, lbc.getCellDepth());
        assertEquals(
                com.lb_calc_web.domain.attributes.PositionControlModule.CENTER,
                als.getPositionControlModule()
        );
        assertEquals(DirectionDoorOpening.LEFT, lbc.getDirectionDoorOpening());
        assertTrue(lbc.getEquipment().isEmpty());
    }
}
