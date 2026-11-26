package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LCGeometryCalculatorTest {

    @Test
    void calculateWidth_shouldReturnMinimumWidthWhenDisplayIsAbsent() {
        assertEquals(200, LCGeometryCalculator.calculateWidth(Map.of()));
    }

    @Test
    void calculateWidth_shouldRoundTenInchDisplayWidthUpToNearestFifty() {
        assertEquals(300, LCGeometryCalculator.calculateWidth(Map.of(display(255, 174), 1)));
    }

    @Test
    void calculateWidth_shouldRoundSeventeenInchDisplayWidthUpToNearestFifty() {
        assertEquals(400, LCGeometryCalculator.calculateWidth(Map.of(display(338, 270), 1)));
    }

    @Test
    void calculateWidth_shouldRoundNineteenInchDisplayWidthUpToNearestFifty() {
        assertEquals(450, LCGeometryCalculator.calculateWidth(Map.of(display(376, 301), 1)));
    }

    @Test
    void calculateDisplayTop_shouldKeepDisplayCenterAtThirteenHundredMillimeters() {
        assertEquals(553, LCGeometryCalculator.calculateDisplayTop(1940, 50, 174));
    }

    @Test
    void calculateDisplayTop_shouldMoveDisplayDownWhenTopClearanceIsInsufficient() {
        assertEquals(70, LCGeometryCalculator.calculateDisplayTop(1400, 50, 301));
    }

    private Equipment display(int width, int height) {
        return new Equipment(
                null,
                EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.CUSTOM,
                EquipmentCategory.DISPLAY,
                "Тестовый дисплей",
                "",
                width,
                height,
                0
        );
    }
}
