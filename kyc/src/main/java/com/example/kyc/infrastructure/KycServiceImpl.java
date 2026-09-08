package com.example.kyc.infrastructure;

import com.example.kyc.application.KycService;
import com.example.kyc.domain.KycSubmission;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class KycServiceImpl implements KycService {
    @Override
    public KycSubmission submitKyc(long userId, String payload) {
        // Minimal stub - persist later in infra repo
        return new KycSubmission(0L, userId, "provider-ref", "PENDING", OffsetDateTime.now());
    }

    @Override
    public void handleProviderCallback(String providerPayload) {
        // For Phase 2, just log and ignore
        System.out.println("KYC provider callback: " + providerPayload);
    }
}
