package com.example.auth.api;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {
}
