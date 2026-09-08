package com.example.auth.api;

import jakarta.validation.constraints.NotBlank;

public class ConfirmPasswordResetRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;

    public ConfirmPasswordResetRequest() {}

    public ConfirmPasswordResetRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() { return token; }
    public String getNewPassword() { return newPassword; }
}
