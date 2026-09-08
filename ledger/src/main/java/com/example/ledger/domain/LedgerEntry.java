package com.example.ledger.domain;

import com.example.shared.Money;

public final class LedgerEntry {
    private final long id;
    private final long transactionId;
    private final long walletId;
    private final Money amount; // signed amount

    public LedgerEntry(long id, long transactionId, long walletId, Money amount) {
        this.id = id;
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.amount = amount;
    }

    public long getId() { return id; }
    public long getTransactionId() { return transactionId; }
    public long getWalletId() { return walletId; }
    public Money getAmount() { return amount; }
}
