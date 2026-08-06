package com.example.studybuddy.validator;

import android.text.TextUtils;
import android.util.Patterns;

public class RegisterFormValidator {

    private static final int MIN_CARACT_PASS = 6;
    private static final int MIN_CARACT_NAME = 2;
    public ValidationResult validateName(String name) {

        if (TextUtils.isEmpty(name)) return ValidationResult.failure("Name is required");

        if (name.length() < MIN_CARACT_NAME) return ValidationResult.failure("Minimum 2 characters");

        return ValidationResult.success();
    }

    public ValidationResult validateEmail(String email) {

        if (TextUtils.isEmpty(email)) return ValidationResult.failure("Email is required");

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return ValidationResult.failure("Invalid email format");

        return ValidationResult.success();
    }

    public ValidationResult validatePassword(String password) {

        if (TextUtils.isEmpty(password)) return ValidationResult.failure("Password is required");

        if (password.length() < MIN_CARACT_PASS) return ValidationResult.failure("Minimum 6 characters");

        return ValidationResult.success();
    }
}