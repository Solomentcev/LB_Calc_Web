package com.lb_calc_web.service.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationResultTest {

    @Test
    void addError_shouldCopyObjectIdentityToValidationError() {
        ValidationResult result =
                new ValidationResult("ALS", 15L);

        result.addError(
                "height",
                "Недопустимая высота",
                10,
                125,
                2300
        );

        ValidationError error = result.getErrors().get(0);

        assertEquals("ALS", error.getObjectType());
        assertEquals(15L, error.getObjectId());
    }

    @Test
    void setObjectId_shouldUpdateExistingValidationErrors() {
        ValidationResult result =
                new ValidationResult("Project", null);

        result.addError(
                "height",
                "Недопустимая высота",
                10,
                125,
                2300
        );

        result.setObjectId(25L);

        ValidationError error = result.getErrors().get(0);

        assertEquals("Project", error.getObjectType());
        assertEquals(25L, error.getObjectId());
    }
}
