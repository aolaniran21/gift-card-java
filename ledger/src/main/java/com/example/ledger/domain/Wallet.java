package com.example.ledger.domain;

import com.example.shared.Money;

public final class Wallet {
    private final long id;
    private final long ownerId;
    private final String currency;

    public Wallet(long id, long ownerId, String currency) {
        this.id = id;
        this.ownerId = ownerId;
        this.currency = currency;
    }

    public long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public String getCurrency() { return currency; }
}
