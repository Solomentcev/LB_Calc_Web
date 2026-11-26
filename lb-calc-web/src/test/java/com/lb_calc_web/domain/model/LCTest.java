package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LCTest {

    @Test
    void recalculate_shouldSetMinimumWidthWhenDisplayIsAbsent() {
        LC lc = createLC(1940, Map.of());

        assertEquals(200, lc.getWidth());
        assertEquals(0, lc.getDisplayTop());
    }

    @Test
    void recalculate_shouldCalculateWidthForTenInchDisplay() {
        LC lc = createLC(1940, Map.of(display(255, 174), 1));

        assertEquals(300, lc.getWidth());
    }

    @Test
    void recalculate_shouldCalculateWidthForSeventeenInchDisplay() {
        LC lc = createLC(1940, Map.of(display(338, 270), 1));

        assertEquals(400, lc.getWidth());
    }

    @Test
    void recalculate_shouldCalculateWidthForNineteenInchDisplay() {
        LC lc = createLC(1940, Map.of(display(376, 301), 1));

        assertEquals(450, lc.getWidth());
        assertEquals(489, lc.getDisplayTop());
    }

    @Test
    void getDisplayTop_shouldKeepDisplayCenterAtThirteenHundredMillimeters() {
        LC lc = createLC(1940, Map.of(display(255, 174), 1));

        assertEquals(553, lc.getDisplayTop());
    }

    @Test
    void getDisplayTop_shouldMoveDisplayDownWhenTopClearanceIsInsufficient() {
        LC lc = createLC(1400, Map.of(display(376, 301), 1));

        assertEquals(70, lc.getDisplayTop());
    }

    private LC createLC(int height, Map<Equipment, Integer> equipment) {
        FunctionalConfiguration functionalConfiguration = new FunctionalConfiguration(
                DisplayPurpose.CUSTOM,
                Set.of(),
                PaymentPurpose.NONE,
                PrinterPurpose.ABSENT
        );
        ControlConfiguration configuration = new ControlConfiguration(
                functionalConfiguration, equipment);
        return new LC(height, 0, 600, 50, 50,
                Colors.Black, Colors.Black, configuration);
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
