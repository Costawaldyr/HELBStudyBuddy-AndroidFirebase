package com.example.studybuddy.validator;

import android.text.TextUtils;
import android.util.Patterns;

public class LoginFormValidator {

    public ValidationResult validate(String email, String password) {

        if (TextUtils.isEmpty(email)) return ValidationResult.failure("Email is required");

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return ValidationResult.failure("Invalid email format");

        if (TextUtils.isEmpty(password)) return ValidationResult.failure("Password is required");

        return ValidationResult.success();
    }
}