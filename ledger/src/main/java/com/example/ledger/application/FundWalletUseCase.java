package com.example.ledger.application;

import com.example.shared.Money;

public interface FundWalletUseCase {
    /**
     * Initiate funding a wallet. Idempotency handled at controller layer.
     */
    void fundWallet(long walletId, long userId, Money amount, String idempotencyKey);
}
