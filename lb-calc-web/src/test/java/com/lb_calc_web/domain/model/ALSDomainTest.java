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
}
