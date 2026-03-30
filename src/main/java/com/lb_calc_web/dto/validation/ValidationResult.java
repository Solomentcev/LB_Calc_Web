package com.lb_calc_web.dto.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private String objectType; // ALS, LB, LC
    private Long objectId;
    private List<ValidationError> errors;
    private LocalDate timestamp;

    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.timestamp = LocalDate.now();
    }

    public ValidationResult(String objectType, Long objectId) {
        this();
        this.objectType = objectType;
        this.objectId = objectId;
    }

    public void addError(String field, String message, Object actual, Object min, Object max) {
        errors.add(new ValidationError(field, message, actual, min, max));
    }

    public void addError(ValidationError error) {
        errors.add(error);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    // Getters and Setters
    public boolean getValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }

    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }

    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }

    public LocalDate getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDate timestamp) { this.timestamp = timestamp; }

    public int getErrorCount() { return errors.size(); }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + isValid() +
                ", objectType='" + objectType + '\'' +
                ", errorCount=" + errors.size() +
                '}';
    }
}
