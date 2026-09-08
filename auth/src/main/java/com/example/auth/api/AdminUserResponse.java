package com.example.auth.api;

public record AdminUserResponse(Long id, String email, String firstName, String lastName, String role) {
}
