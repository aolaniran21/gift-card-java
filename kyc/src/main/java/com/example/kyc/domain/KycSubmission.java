package com.example.kyc.domain;

import java.time.OffsetDateTime;

public final class KycSubmission {
    private final long id;
    private final long userId;
    private final String providerRef;
    private final String status; // PENDING, VERIFIED, REJECTED
    private final OffsetDateTime createdAt;

    public KycSubmission(long id, long userId, String providerRef, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.providerRef = providerRef;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getProviderRef() { return providerRef; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
