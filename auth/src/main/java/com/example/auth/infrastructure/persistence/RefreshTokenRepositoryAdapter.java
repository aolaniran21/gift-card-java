package com.example.auth.infrastructure.persistence;

import com.example.auth.domain.RefreshToken;
import com.example.auth.domain.RefreshTokenRepository;
import com.example.auth.infrastructure.security.RefreshTokenCrypto;
import com.example.auth.infrastructure.security.RefreshTokenConstants;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpa;
    private final RefreshTokenCrypto crypto;

    public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository jpa, RefreshTokenCrypto crypto) {
        this.jpa = jpa;
        this.crypto = crypto;
    }

    @Override
    public Optional<RefreshToken> findByToken(String rawToken, String presentedDeviceId) {
        if (rawToken == null || rawToken.isBlank()) return Optional.empty();
        // expected format: tokenId.expiresEpoch.signatureBase64Url
        String[] parts = rawToken.split("\\.");
        if (parts.length != 3) return Optional.empty();
        String tokenId = parts[0];
        String expiresEpochStr = parts[1];
        String sig = parts[2];

        Optional<RefreshTokenEntity> maybe = jpa.findByToken(tokenId);
        if (maybe.isEmpty()) return Optional.empty();
        RefreshTokenEntity entity = maybe.get();

        long parsedEpoch;
        try {
            parsedEpoch = Long.parseLong(expiresEpochStr);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        // ensure stored expires matches token payload (prevents tampering with expiry)
        Instant storedExpires = entity.getExpiresAt();
        if (storedExpires == null || storedExpires.getEpochSecond() != parsedEpoch) {
            return Optional.empty();
        }

        String data = entity.getDeviceId() == null
                ? tokenId + ":" + expiresEpochStr + ":" + entity.getEmail()
                : tokenId + ":" + expiresEpochStr + ":" + entity.getEmail() + ":" + entity.getDeviceId();
        if (!crypto.verifySignatureBase64Url(data, sig)) {
            return Optional.empty();
        }

        // check device binding: presented deviceId must match stored deviceId
        // Legacy sentinel: allow any presented deviceId for legacy tokens so old tokens keep working.
        String storedDevice = entity.getDeviceId();
        if (RefreshTokenConstants.LEGACY_DEVICE_ID.equals(storedDevice)) {
            // accept any presentedDeviceId for legacy tokens
        } else if (storedDevice != null) {
            if (presentedDeviceId == null || !storedDevice.equals(presentedDeviceId)) {
                // device mismatch -> treat as invalid/malformed token
                return Optional.empty();
            }
        } else {
            // shouldn't happen once migration applied, but be defensive
            if (presentedDeviceId != null) return Optional.empty();
        }

        // check revoked/expired
        if (entity.getRevokedAt() != null) {
            // signature valid but token was revoked -> possible reuse attack
            // If this is the first time reuse is observed, mark it and raise the special exception.
            // Subsequent presentations of the same revoked token will return empty (treated as unknown/malformed)
            if (!entity.isRevocationNotified()) {
                entity.setRevocationNotified(true);
                jpa.save(entity);
                throw new com.example.auth.infrastructure.security.TokenReuseDetectedException(entity.getEmail());
            } else {
                // Already notified -> treat as missing/invalid token
                return Optional.empty();
            }
        }
        if (entity.getExpiresAt().isBefore(Instant.now())) return Optional.empty();

        return Optional.of(toDomainWithMaskedToken(entity));
    }

    @Override
    public void deleteByEmail(String email) {
        jpa.deleteByEmail(email);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        // If token is null (update-only, e.g., revoke), update existing entity by id
        if (refreshToken.getToken() == null && refreshToken.getId() != null) {
            return jpa.findById(refreshToken.getId()).map(existing -> {
                if (refreshToken.getRevokedAt() != null) existing.setRevokedAt(refreshToken.getRevokedAt());
                if (refreshToken.getExpiresAt() != null) existing.setExpiresAt(refreshToken.getExpiresAt());
                if (refreshToken.getDeviceId() != null) existing.setDeviceId(refreshToken.getDeviceId());
                RefreshTokenEntity saved = jpa.save(existing);
                return new RefreshToken(saved.getId(), null, saved.getEmail(), saved.getExpiresAt(), saved.getRevokedAt(), saved.getDeviceId());
            }).orElseGet(() -> {
                RefreshTokenEntity e = toEntity(refreshToken);
                RefreshTokenEntity saved = jpa.save(e);
                return new RefreshToken(saved.getId(), refreshToken.getToken(), saved.getEmail(), saved.getExpiresAt(), saved.getRevokedAt(), saved.getDeviceId());
            });
        }

        // Create path: refreshToken.getToken() is expected to be the tokenId for storage
        RefreshTokenEntity entity = toEntity(refreshToken);
        // Ensure deviceId is set on creation (should be provided for new tokens)
        if (entity.getDeviceId() == null) {
            entity.setDeviceId(com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID);
        }
        RefreshTokenEntity saved = jpa.save(entity);
        // return domain object (mask raw token)
        return new RefreshToken(saved.getId(), null, saved.getEmail(), saved.getExpiresAt(), saved.getRevokedAt(), saved.getDeviceId());
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        if (refreshToken.getId() != null) {
            jpa.deleteById(refreshToken.getId());
        } else if (refreshToken.getToken() != null) {
            jpa.findByToken(refreshToken.getToken()).ifPresent(jpa::delete);
        }
    }

    private RefreshToken toDomainWithMaskedToken(RefreshTokenEntity e) {
        // Do not expose the token signature; return domain token as null to avoid leaking
        return new RefreshToken(e.getId(), null, e.getEmail(), e.getExpiresAt(), e.getRevokedAt());
    }

    private RefreshTokenEntity toEntity(RefreshToken r) {
        RefreshTokenEntity e = new RefreshTokenEntity(r.getToken(), r.getEmail(), r.getExpiresAt());
        if (r.getId() != null) {
            e.setId(r.getId());
        }
        if (r.getRevokedAt() != null) {
            e.setRevokedAt(r.getRevokedAt());
        }
        if (r.getDeviceId() != null) {
            e.setDeviceId(r.getDeviceId());
        }
        if (r.getCreatedAt() != null) {
            e.setCreatedAt(r.getCreatedAt());
        }
        return e;
    }
}
