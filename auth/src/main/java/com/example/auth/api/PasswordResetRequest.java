package com.example.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequest {
    @Email
    @NotBlank
    private String email;

    public PasswordResetRequest() {}

    public PasswordResetRequest(String email) { this.email = email; }

    public String getEmail() { return email; }
}
