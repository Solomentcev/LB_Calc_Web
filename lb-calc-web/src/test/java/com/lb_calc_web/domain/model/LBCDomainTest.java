
package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Display;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LBCDomainTest {

    @Test
    void lbcActsAsControlAndStorageModule() {
        ControlConfiguration control =
                new ControlConfiguration(
                        Payment.NONE,
                        Set.<AccessMethod>of(),
                        Set.<PrintOption>of(),
                        new EquipmentConfiguration(
                                List.of(Display.LC10),
                                List.of()
                        )
                );

        LBC lbc =
                new LBC(
                        1940,
                        500,
                        600,
                        100,
                        100,
                        Colors.Blue,
                        Colors.White,
                        new TypeLb(
                                "TYPE1",
                                100,
                                5,
                                20
                        ),
                        10,
                        DirectionDoorOpening.LEFT,
                        20,
                        control
                );

        ALS als =
                new ALS(
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
        assertEquals(
                com.lb_calc_web.domain.attributes.PositionControlModule.CENTER,
                als.getPositionControlModule()
        );
        assertEquals(
                DirectionDoorOpening.LEFT,
                lbc.getDirectionDoorOpening()
        );
    }
}
