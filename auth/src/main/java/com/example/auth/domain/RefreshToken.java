package com.example.auth.domain;

import java.time.Instant;

public class RefreshToken {

    private Long id;
    private String token;
    private String email;
    private Instant expiresAt;
    private Instant revokedAt;
    private String deviceId;
    private Instant createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(Long id, String token, String email, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = Instant.now();
    }

    public RefreshToken(Long id, String token, String email, Instant expiresAt, Instant revokedAt, String deviceId) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.deviceId = deviceId;
        this.createdAt = Instant.now();
    }

    public RefreshToken(Long id, String token, String email, Instant expiresAt, Instant revokedAt, String deviceId, Instant createdAt) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.deviceId = deviceId;
        this.createdAt = createdAt;
    }

    public RefreshToken(String token, String email, Instant expiresAt) {
        this(null, token, email, expiresAt, null, null, Instant.now());
    }

    public RefreshToken(String token, String email, Instant expiresAt, String deviceId) {
        this(null, token, email, expiresAt, null, deviceId, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getDeviceId() { return deviceId; }
 
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }
}
