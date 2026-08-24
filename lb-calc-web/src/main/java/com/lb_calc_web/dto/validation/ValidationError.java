package com.lb_calc_web.dto.validation;

public class ValidationError {
    private String objectType; // ALS, LB, LC
    private Long objectId; // ID объекта, если применимо
    private String field;
    private String message;
    private Object actualValue;
    private Object minValue;
    private Object maxValue;

    public ValidationError() {}

    public ValidationError(String field, String message, Object actualValue,
                           Object minValue, Object maxValue) {
        this.field = field;
        this.message = message;
        this.actualValue = actualValue;
        this.minValue = minValue;
        this.maxValue = maxValue;

    }
    public ValidationError(String objectType,Long objectId, String field, String message, Object actualValue,
                           Object minValue, Object maxValue) {
        this.objectType = objectType;
        this.objectId = objectId;
        this.field = field;
        this.message = message;
        this.actualValue = actualValue;
        this.minValue = minValue;
        this.maxValue = maxValue;

    }

    // Getters and Setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getActualValue() { return actualValue; }
    public void setActualValue(Object actualValue) { this.actualValue = actualValue; }

    public Object getMinValue() { return minValue; }
    public void setMinValue(Object minValue) { this.minValue = minValue; }

    public Object getMaxValue() { return maxValue; }
    public void setMaxValue(Object maxValue) { this.maxValue = maxValue; }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "["+objectType+"-id"+objectId+"]"+field + ": " + message +
                " (текущее: " + actualValue + ", допустимое: " + minValue + "-" + maxValue + ")";
    }
}
