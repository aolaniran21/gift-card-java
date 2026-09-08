package com.example.ledger.ports;

import com.example.ledger.domain.Transaction;

public interface LedgerRepository {
    Transaction createTransaction(Transaction tx);
}
