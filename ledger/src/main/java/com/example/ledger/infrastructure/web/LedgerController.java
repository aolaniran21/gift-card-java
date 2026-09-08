package com.example.ledger.infrastructure.web;

import com.example.shared.ApiResponse;
import com.example.shared.Money;
import com.example.ledger.application.FundWalletUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class LedgerController {
    private final FundWalletUseCase fundWalletUseCase;

    public LedgerController(FundWalletUseCase fundWalletUseCase) {
        this.fundWalletUseCase = fundWalletUseCase;
    }

    public static record FundRequest(@Min(1) long amountKobo, @Min(1) long walletId) {}

    @PostMapping("/fund")
    public ResponseEntity<ApiResponse<String>> fund(@Valid @RequestBody FundRequest req, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var money = new Money(req.amountKobo, "NGN");
        fundWalletUseCase.fundWallet(req.walletId, 0L, money, idempotencyKey);
        return ResponseEntity.accepted().body(new ApiResponse<>("Funding initiated", null));
    }
}
