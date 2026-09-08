package com.example.auth.infrastructure.persistence;

import com.example.auth.domain.RefreshToken;
import com.example.auth.infrastructure.security.RefreshTokenCrypto;
import com.example.auth.infrastructure.security.RefreshTokenGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenRepositoryAdapterTest {

    @Mock
    JpaRefreshTokenRepository jpa;

    @Test
    public void findByToken_validToken_returnsDomain() {
        String base64Secret = "ZmFrZV9oYWNrc2VjcmV0X3Rlc3Q=";
        RefreshTokenCrypto crypto = new RefreshTokenCrypto(base64Secret);
        RefreshTokenRepositoryAdapter adapter = new RefreshTokenRepositoryAdapter(jpa, crypto);

        String tokenId = "tid-123";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String email = "alice@example.com";

        RefreshTokenEntity entity = new RefreshTokenEntity(tokenId, email, expiresAt);
        // saved entity uses token field == tokenId
        when(jpa.findByToken(tokenId)).thenReturn(Optional.of(entity));

        // construct raw token matching signed format
        String data = tokenId + ":" + expiresAt.getEpochSecond() + ":" + email;
        String sig = crypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expiresAt.getEpochSecond() + "." + sig;

        Optional<RefreshToken> maybe = adapter.findByToken(raw, null);
        assertTrue(maybe.isPresent());
        RefreshToken rt = maybe.get();
        assertEquals(email, rt.getEmail());
        assertNull(rt.getToken(), "Adapter should mask raw token in domain object");
    }

    @Test
    public void findByToken_badSignature_returnsEmpty() {
        String base64Secret = "ZmFrZV9oYWNrc2VjcmV0X3Rlc3Q=";
        RefreshTokenCrypto crypto = new RefreshTokenCrypto(base64Secret);
        RefreshTokenRepositoryAdapter adapter = new RefreshTokenRepositoryAdapter(jpa, crypto);

        String tokenId = "tid-999";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String email = "bob@example.com";

        RefreshTokenEntity entity = new RefreshTokenEntity(tokenId, email, expiresAt);
        when(jpa.findByToken(tokenId)).thenReturn(Optional.of(entity));

        // tampered signature
        String raw = tokenId + "." + expiresAt.getEpochSecond() + "." + "invalidsig";

        Optional<RefreshToken> maybe = adapter.findByToken(raw, null);
        assertTrue(maybe.isEmpty());
    }

    @Test
    public void findByToken_revoked_throwsReuseDetected() {
        String base64Secret = "ZmFrZV9oYWNrc2VjcmV0X3Rlc3Q=";
        RefreshTokenCrypto crypto = new RefreshTokenCrypto(base64Secret);
        RefreshTokenRepositoryAdapter adapter = new RefreshTokenRepositoryAdapter(jpa, crypto);

        String tokenId = "tid-777";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String email = "carol@example.com";

        RefreshTokenEntity entity = new RefreshTokenEntity(tokenId, email, expiresAt);
        entity.setRevokedAt(Instant.now());
        when(jpa.findByToken(tokenId)).thenReturn(Optional.of(entity));

        String data = tokenId + ":" + expiresAt.getEpochSecond() + ":" + email;
        String sig = crypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expiresAt.getEpochSecond() + "." + sig;

        assertThrows(com.example.auth.infrastructure.security.TokenReuseDetectedException.class, () -> adapter.findByToken(raw, null));

    }

    @Test
    public void findByToken_withDevice_bindingEnforced() {
        String base64Secret = "ZmFrZV9oYWNrc2VjcmV0X3Rlc3Q=";
        RefreshTokenCrypto crypto = new RefreshTokenCrypto(base64Secret);
        RefreshTokenRepositoryAdapter adapter = new RefreshTokenRepositoryAdapter(jpa, crypto);

        String tokenId = "tid-555";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String email = "dev@example.com";
        String deviceId = "device-abc";

        RefreshTokenEntity entity = new RefreshTokenEntity(tokenId, email, expiresAt);
        entity.setDeviceId(deviceId);
        when(jpa.findByToken(tokenId)).thenReturn(Optional.of(entity));

        String data = tokenId + ":" + expiresAt.getEpochSecond() + ":" + email + ":" + deviceId;
        String sig = crypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expiresAt.getEpochSecond() + "." + sig;

        // correct device
        Optional<RefreshToken> maybeOk = adapter.findByToken(raw, deviceId);
        assertTrue(maybeOk.isPresent());

        // incorrect device
        Optional<RefreshToken> maybeBad = adapter.findByToken(raw, "other-device");
        assertTrue(maybeBad.isEmpty());
    }
}
