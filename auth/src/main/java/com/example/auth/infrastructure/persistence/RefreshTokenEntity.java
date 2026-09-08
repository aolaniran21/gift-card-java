package com.example.auth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant revokedAt;

    @Column(nullable = false)
    private boolean revocationNotified = false;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "created_at")
    private Instant createdAt;

    protected RefreshTokenEntity() {
    }

    public RefreshTokenEntity(String token, String email, Instant expiresAt) {
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getToken() {
        return token;
    }

    public void setToken(String token) { this.token = token; }

    public String getEmail() {
        return email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public boolean isRevocationNotified() { return revocationNotified; }

    public void setRevocationNotified(boolean revocationNotified) { this.revocationNotified = revocationNotified; }

    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
