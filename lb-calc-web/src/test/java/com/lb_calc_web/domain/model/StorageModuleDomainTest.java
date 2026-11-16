package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageModuleDomainTest {

    @Test
    void recalculateCells_shouldSubtractServiceZoneWidth() {
        LB lb = createLB(10);

        assertEquals(380, lb.getCellWidth());
    }

    @Test
    void setCountCells_shouldRejectMoreThanTwentyCells() {
        LB lb = createLB(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> lb.setCountCells(21)
        );

        assertEquals(10, lb.getCountCells());
    }

    @Test
    void setCountCells_shouldRestorePreviousStateWhenCellHeightIsTooSmall() {
        LB lb = createLB(10);

        assertThrows(
                IllegalStateException.class,
                () -> lb.setCountCells(20)
        );

        assertEquals(10, lb.getCountCells());
        assertEquals(169, lb.getCellHeight());
    }

    private LB createLB(int countCells) {
        return new LB(
                1940,
                500,
                600,
                100,
                100,
                Colors.Blue,
                Colors.White,
                new TypeLb("TYPE1", 100, 5, 20),
                countCells,
                DirectionDoorOpening.LEFT,
                20
        );
    }
}
