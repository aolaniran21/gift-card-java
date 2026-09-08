package com.example.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByEmail(String email);

    // Find tokens by device id (used by maintenance tasks)
    List<RefreshTokenEntity> findByDeviceId(String deviceId);

    List<RefreshTokenEntity> findByDeviceIdAndCreatedAtBefore(String deviceId, Instant cutoff);
}
