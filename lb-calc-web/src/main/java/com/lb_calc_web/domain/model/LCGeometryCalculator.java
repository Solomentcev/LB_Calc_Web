package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;

import java.util.Map;

/** Рассчитывает геометрию LC на основании установленного дисплея. */
public final class LCGeometryCalculator {

    private static final int MIN_WIDTH = 200;
    private static final int SIDE_CLEARANCE = 20;
    private static final int WIDTH_STEP = 50;
    private static final int DISPLAY_CENTER_HEIGHT = 1300;
    private static final int TOP_CLEARANCE = 20;

    private LCGeometryCalculator() {
    }

    /**
     * Рассчитывает ширину корпуса LC: не менее 200 мм без дисплея либо
     * ширина дисплея с боковыми зазорами по 20 мм, округлённая вверх до 50 мм.
     *
     * @param equipment установленное оборудование
     * @return расчётная ширина корпуса, мм
     */
    public static int calculateWidth(Map<Equipment, Integer> equipment) {
        Equipment display = findDisplay(equipment);
        if (display == null) {
            return MIN_WIDTH;
        }

        long requiredWidth = Math.max(
                MIN_WIDTH,
                (long) display.getWidth() + 2L * SIDE_CLEARANCE
        );
        long roundedWidth = ((requiredWidth + WIDTH_STEP - 1) / WIDTH_STEP) * WIDTH_STEP;
        return Math.toIntExact(roundedWidth);
    }

    /**
     * Рассчитывает положение верхнего края дисплея относительно верхнего края LC.
     * Центр дисплея располагается на высоте 1300 мм от низа; при недостаточном
     * зазоре до верхней рамы дисплей смещается вниз.
     *
     * @param moduleHeight высота LC, мм
     * @param upperFrame высота верхней рамы, мм
     * @param displayHeight высота дисплея, мм
     * @return координата верхнего края дисплея от верха LC, мм
     */
    public static int calculateDisplayTop(int moduleHeight, int upperFrame, int displayHeight) {
        if (displayHeight <= 0) {
            return 0;
        }

        int centeredTop = moduleHeight - DISPLAY_CENTER_HEIGHT - displayHeight / 2;
        return Math.max(centeredTop, upperFrame + TOP_CLEARANCE);
    }

    private static Equipment findDisplay(Map<Equipment, Integer> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return null;
        }
        return equipment.keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.DISPLAY)
                .findFirst()
                .orElse(null);
    }
}
