package com.example.ledger.infrastructure.payment;

import com.example.shared.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockPaymentProcessor {
    private static final Logger log = LoggerFactory.getLogger(MockPaymentProcessor.class);

    public boolean charge(long walletId, long userId, Money amount) {
        log.info("Mock charging wallet {} user {} amount {}", walletId, userId, amount);
        // Simulate success always for Phase 2
        return true;
    }
}
