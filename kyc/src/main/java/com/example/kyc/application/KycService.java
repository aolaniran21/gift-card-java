package com.example.kyc.application;

import com.example.kyc.domain.KycSubmission;

public interface KycService {
    KycSubmission submitKyc(long userId, String payload);
    void handleProviderCallback(String providerPayload);
}
