package com.lb_calc_web.handler;
import com.lb_calc_web.dto.validation.ValidationResult;

public class ValidationSizeException extends RuntimeException {
    private final ValidationResult validationResult;

    public ValidationSizeException (ValidationResult validationResult) {
        super("Validation failed for " + validationResult.getObjectType());
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}