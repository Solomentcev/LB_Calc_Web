package com.lb_calc_web.dto.validation;

public class ValidationError {
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


    @Override
    public String toString() {
        return field + ": " + message +
                " (текущее: " + actualValue + ", допустимое: " + minValue + "-" + maxValue + ")";
    }
}
