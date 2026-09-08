package com.example.shared;

import java.time.OffsetDateTime;

public final class AuditEntry {
    private final long id;
    private final String aggregateType;
    private final long aggregateId;
    private final String action;
    private final String payload;
    private final OffsetDateTime createdAt;

    public AuditEntry(long id, String aggregateType, long aggregateId, String action, String payload, OffsetDateTime createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.action = action;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public long getAggregateId() { return aggregateId; }
    public String getAction() { return action; }
    public String getPayload() { return payload; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
