package com.example.auth.infrastructure.security;

import com.example.auth.domain.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RefreshTokenGenerator {

    private final RefreshTokenCrypto crypto;
    private final long defaultExpirySeconds;

    public RefreshTokenGenerator(RefreshTokenCrypto crypto,
                                 @Value("${refresh.token.expiration-seconds:86400}") long defaultExpirySeconds) {
        this.crypto = crypto;
        this.defaultExpirySeconds = defaultExpirySeconds;
    }

    public GeneratedRefreshToken generate(String email) {
        return generate(email, Instant.now().plusSeconds(defaultExpirySeconds), null);
    }

    public GeneratedRefreshToken generate(String email, Instant expiresAt) {
        return generate(email, expiresAt, null);
    }

    public GeneratedRefreshToken generate(String email, String deviceId) {
        Instant expiresAt = Instant.now().plusSeconds(defaultExpirySeconds);
        return generate(email, expiresAt, deviceId);
    }

    public GeneratedRefreshToken generate(String email, Instant expiresAt, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId is required for refresh token generation");
        }
        String tokenId = UUID.randomUUID().toString();
        String expiresEpoch = String.valueOf(expiresAt.getEpochSecond());
        String data = deviceId == null
                ? tokenId + ":" + expiresEpoch + ":" + email
                : tokenId + ":" + expiresEpoch + ":" + email + ":" + deviceId;
        String sig = crypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expiresEpoch + "." + sig;

        RefreshToken domain = new RefreshToken(null, tokenId, email, expiresAt, null, deviceId);
        return new GeneratedRefreshToken(raw, tokenId, domain);
    }

    public static class GeneratedRefreshToken {
        private final String rawToken;
        private final String tokenId;
        private final RefreshToken refreshTokenDomain;

        public GeneratedRefreshToken(String rawToken, String tokenId, RefreshToken refreshTokenDomain) {
            this.rawToken = rawToken;
            this.tokenId = tokenId;
            this.refreshTokenDomain = refreshTokenDomain;
        }

        public String getRawToken() { return rawToken; }
        public String getTokenId() { return tokenId; }
        public RefreshToken getRefreshTokenDomain() { return refreshTokenDomain; }
    }
}
