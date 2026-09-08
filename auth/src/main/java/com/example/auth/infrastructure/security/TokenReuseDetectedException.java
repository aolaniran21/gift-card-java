package com.example.auth.infrastructure.security;

public class TokenReuseDetectedException extends RuntimeException {
    private final String email;

    public TokenReuseDetectedException(String email) {
        super("Refresh token reuse detected for user: " + email);
        this.email = email;
    }

    public String getEmail() { return email; }
}
