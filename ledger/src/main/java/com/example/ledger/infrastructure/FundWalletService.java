package com.example.ledger.infrastructure;

import com.example.ledger.application.FundWalletUseCase;
import com.example.shared.Money;
import org.springframework.stereotype.Service;

@Service
public class FundWalletService implements FundWalletUseCase {
    @Override
    public void fundWallet(long walletId, long userId, Money amount, String idempotencyKey) {
        // Phase 2 stub: record outbox, idempotency, ledger entries in real impl
        System.out.printf("fundWallet invoked wallet=%d user=%d amount=%s idempotency=%s\n", walletId, userId, amount, idempotencyKey);
    }
}
