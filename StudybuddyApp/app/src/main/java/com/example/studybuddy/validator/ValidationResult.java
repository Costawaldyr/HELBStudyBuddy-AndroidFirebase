package com.example.studybuddy.validator;

public class ValidationResult {

    private final boolean valid;
    private final String error;

    private ValidationResult(boolean valid, String error) {
        this.valid = valid;
        this.error = error;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String error) {
        return new ValidationResult(false, error);
    }

    public boolean isValid() {
        return valid;
    }

    public String getError() {
        return error;
    }
}