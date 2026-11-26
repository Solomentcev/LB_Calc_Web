package com.lb_calc_web.domain.model;

/** Настройки физических ограничений геометрии управляющего модуля LC. */
public record LCGeometrySettings(
        int minimumWidth,
        int displaySideClearance,
        int widthRoundingStep,
        int displayCenterHeight,
        int displayTopClearance
) {
    public LCGeometrySettings {
        if (minimumWidth <= 0 || displaySideClearance < 0 || widthRoundingStep <= 0
                || displayCenterHeight < 0 || displayTopClearance < 0) {
            throw new IllegalArgumentException("Настройки геометрии LC содержат недопустимые значения");
        }
    }
}
