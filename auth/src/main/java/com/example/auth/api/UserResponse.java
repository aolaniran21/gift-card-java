package com.example.auth.api;

public record UserResponse(Long id, String email, String firstName, String lastName, String role) {
}
