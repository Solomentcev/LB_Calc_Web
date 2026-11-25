package com.lb_calc_web.controller;

import com.lb_calc_web.service.validation.ValidationError;
import com.lb_calc_web.service.validation.ValidationResult;

import java.util.List;

/**
 * Форматирует прикладные ошибки в текстовое представление для MVC и REST.
 */
public final class ValidationErrorFormatter {

    private ValidationErrorFormatter() {
    }

    /** Преобразует результат геометрической проверки в список сообщений для представления. */
    public static List<String> format(ValidationResult result) {
        if (result == null) {
            return List.of();
        }

        return result.getErrors().stream()
                .map(ValidationErrorFormatter::format)
                .toList();
    }

    private static String format(ValidationError error) {
        return "[" + error.getObjectType() + "-id" + error.getObjectId() + "]"
                + error.getField() + ": " + error.getMessage()
                + " (текущее: " + error.getActualValue()
                + ", допустимое: " + error.getMinValue()
                + "-" + error.getMaxValue() + ")";
    }
}
