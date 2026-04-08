package com.lb_calc_web.handler;
import com.lb_calc_web.dto.validation.ValidationError;
import com.lb_calc_web.dto.validation.ValidationResult;

import java.util.List;

public class ValidationSizeException extends RuntimeException {
    private final ValidationResult validationResult;

    public ValidationSizeException (ValidationResult validationResult) {
        super("Validation failed for " + validationResult.getObjectType());
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
    public List<String> getErrors() {
        return validationResult.getErrors().stream()
                .map(ValidationError::toString)
                .toList();
    }
    public List<String> getErrors(String objectType) {
       return validationResult.getErrors().stream()
                .filter(error -> error.getField().startsWith(objectType))
                .map(ValidationError::toString)
                .toList();

    }
}