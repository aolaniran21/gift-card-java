package com.example.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class RefreshTokenCrypto {

    private final byte[] secret;
    private static final String HMAC_ALGO = "HmacSHA256";

    public RefreshTokenCrypto(@Value("${refresh.token.hmac-secret:ZmFrZV9yZWZyZXNoX3NlY3JldF9zaG91bGRfYmVfZm9yX3Rlc3Rpbmc=}") String base64Secret) {
        this.secret = Base64.getDecoder().decode(base64Secret);
    }

    public String computeSignatureBase64Url(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret, HMAC_ALGO));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    public boolean verifySignatureBase64Url(String data, String providedSig) {
        String expected = computeSignatureBase64Url(data);
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = providedSig.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
