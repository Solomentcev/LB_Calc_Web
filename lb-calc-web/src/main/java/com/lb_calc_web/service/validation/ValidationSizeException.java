package com.lb_calc_web.service.validation;
import java.util.List;

/**
 * Сигнализирует о нарушении ограничений при проверке доменной конфигурации.
 *
 * <p>Исключение относится к прикладной валидации и не зависит от HTTP-слоя.</p>
 */
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