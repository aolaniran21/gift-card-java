package com.example.auth.infrastructure.service;

import com.example.auth.infrastructure.persistence.JpaRefreshTokenRepository;
import com.example.auth.infrastructure.persistence.RefreshTokenEntity;
import com.example.auth.infrastructure.security.RefreshTokenConstants;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class LegacyRefreshTokenRevoker {

    private final JpaRefreshTokenRepository jpa;

    public LegacyRefreshTokenRevoker(JpaRefreshTokenRepository jpa) {
        this.jpa = jpa;
    }

    @Transactional
    public int revokeAllLegacyTokens() {
        List<RefreshTokenEntity> legacy = jpa.findByDeviceId(RefreshTokenConstants.LEGACY_DEVICE_ID);
        int count = 0;
        Instant now = Instant.now();
        for (RefreshTokenEntity e : legacy) {
            if (e.getRevokedAt() == null) {
                e.setRevokedAt(now);
                jpa.save(e);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public int revokeLegacyTokensOlderThanDays(int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        List<RefreshTokenEntity> legacy = jpa.findByDeviceIdAndCreatedAtBefore(RefreshTokenConstants.LEGACY_DEVICE_ID, cutoff);
        int count = 0;
        Instant now = Instant.now();
        for (RefreshTokenEntity e : legacy) {
            if (e.getRevokedAt() == null) {
                e.setRevokedAt(now);
                jpa.save(e);
                count++;
            }
        }
        return count;
    }

    @Transactional(readOnly = true)
    public int countLegacyTokensOlderThanDays(int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        List<RefreshTokenEntity> legacy = jpa.findByDeviceIdAndCreatedAtBefore(RefreshTokenConstants.LEGACY_DEVICE_ID, cutoff);
        return legacy.size();
    }
}
