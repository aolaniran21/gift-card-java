package com.example.auth.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RefreshTokenCryptoTest {

    @Test
    public void computeAndVerifySignature_roundtrip() {
        String base64Secret = "ZmFrZV9oYWNrc2VjcmV0X3Rlc3Q="; // base64 for a test secret
        RefreshTokenCrypto crypto = new RefreshTokenCrypto(base64Secret);

        String data = "token123:1630000000:user@example.com";
        String sig = crypto.computeSignatureBase64Url(data);
        assertNotNull(sig);
        assertTrue(sig.length() > 0);

        assertTrue(crypto.verifySignatureBase64Url(data, sig));
        assertFalse(crypto.verifySignatureBase64Url(data + "x", sig));
        assertFalse(crypto.verifySignatureBase64Url(data, sig + "x"));
    }
}
