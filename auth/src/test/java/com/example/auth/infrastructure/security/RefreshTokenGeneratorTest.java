package com.example.auth.infrastructure.security;

import com.example.auth.domain.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class RefreshTokenGeneratorTest {

    @Test
    public void generateToken_hasExpectedFormat_andDomainMatches() {
        String base64Secret = "ZmFrZV9oYWNrc2VjcmV0X3Rlc3Q=";
        RefreshTokenCrypto crypto = new RefreshTokenCrypto(base64Secret);
        RefreshTokenGenerator generator = new RefreshTokenGenerator(crypto, 3600);

        String email = "user@example.com";

        // also verify deviceId path
        String deviceId = "device-123";
        RefreshTokenGenerator.GeneratedRefreshToken g2 = generator.generate(email, deviceId);
        assertNotNull(g2);
        String raw2 = g2.getRawToken();
        String[] parts2 = raw2.split("\\.");
        String tokenId2 = parts2[0];
        String data2 = tokenId2 + ":" + parts2[1] + ":" + email + ":" + deviceId;
        assertTrue(crypto.verifySignatureBase64Url(data2, parts2[2]));

        // verify domain and signature for device-bound token
        RefreshToken domain = g2.getRefreshTokenDomain();
        assertNotNull(domain);
        assertEquals(tokenId2, domain.getToken());
        assertEquals(email, domain.getEmail());
        assertEquals(Long.parseLong(parts2[1]), domain.getExpiresAt().getEpochSecond());

        // Verify signature via crypto for device-bound token
        String data = tokenId2 + ":" + parts2[1] + ":" + email + ":" + deviceId;
        assertTrue(crypto.verifySignatureBase64Url(data, parts2[2]));
    }
}
