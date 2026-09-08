package com.example.ledger.domain;

import java.time.OffsetDateTime;
import java.util.List;

public final class Transaction {
    private final long id;
    private final String type; // FUND, WITHDRAW
    private final String status; // PENDING, SETTLED, FAILED
    private final OffsetDateTime createdAt;
    private final List<LedgerEntry> entries;

    public Transaction(long id, String type, String status, OffsetDateTime createdAt, List<LedgerEntry> entries) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.entries = entries;
    }

    public long getId() { return id; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<LedgerEntry> getEntries() { return entries; }
}
