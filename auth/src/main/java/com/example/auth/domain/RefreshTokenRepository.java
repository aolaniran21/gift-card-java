package com.example.auth.domain;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token, String deviceId);
    void deleteByEmail(String email);
    RefreshToken save(RefreshToken refreshToken);
    void delete(RefreshToken refreshToken);
}
