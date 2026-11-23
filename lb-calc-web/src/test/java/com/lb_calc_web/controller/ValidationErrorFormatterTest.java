package com.lb_calc_web.controller;

import com.lb_calc_web.service.validation.ValidationError;
import com.lb_calc_web.service.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверяет форматирование прикладных ошибок на границе представления.
 */
class ValidationErrorFormatterTest {

    @Test
    void format_shouldPreserveExistingErrorText() {
        ValidationResult result = new ValidationResult("LB", 15L);
        result.addError(
                new ValidationError(
                        "LB",
                        15L,
                        "height",
                        "Высота вне допустимого диапазона",
                        10,
                        100,
                        200
                )
        );

        assertEquals(
                List.of("[LB-id15]height: Высота вне допустимого диапазона"
                        + " (текущее: 10, допустимое: 100-200)"),
                ValidationErrorFormatter.format(result)
        );
    }

    @Test
    void format_nullResult_shouldReturnEmptyList() {
        assertEquals(List.of(), ValidationErrorFormatter.format(null));
    }
}
