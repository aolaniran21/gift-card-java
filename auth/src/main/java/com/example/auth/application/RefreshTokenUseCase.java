package com.example.auth.application;

import com.example.auth.api.AuthResponse;
import com.example.auth.api.UserResponse;
import com.example.auth.domain.RefreshToken;
import com.example.auth.domain.RefreshTokenRepository;
import com.example.auth.domain.User;
import com.example.auth.domain.UserRepository;
import com.example.shared.JwtUtil;
import com.example.auth.infrastructure.security.RefreshTokenGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final com.example.auth.infrastructure.service.RefreshTokenRevocationService revocationService;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, JwtUtil jwtUtil, RefreshTokenGenerator refreshTokenGenerator, com.example.auth.infrastructure.service.RefreshTokenRevocationService revocationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.revocationService = revocationService;
    }

    @Transactional
    public AuthResponse execute(String refreshTokenValue, String deviceId) {
        RefreshToken refreshToken;
        try {
            refreshToken = refreshTokenRepository.findByToken(refreshTokenValue, deviceId)
                    .orElseThrow(() -> new com.example.auth.infrastructure.security.InvalidRefreshTokenException("Invalid or expired refresh token"));
        } catch (com.example.auth.infrastructure.security.TokenReuseDetectedException e) {
            // revoke all tokens for this user immediately in a separate transaction and rethrow to be handled as auth failure
            revocationService.revokeAllForEmail(e.getEmail());
            throw e;
        }

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new com.example.auth.infrastructure.security.InvalidRefreshTokenException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(refreshToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Revoke the old token (mark revoked) to enable reuse-detection later
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // Generate a new HMAC-backed refresh token
        RefreshTokenGenerator.GeneratedRefreshToken generated = refreshTokenGenerator.generate(user.getEmail(), deviceId);
        String newRefreshTokenRaw = generated.getRawToken();
        RefreshToken toSave = generated.getRefreshTokenDomain();
        refreshTokenRepository.save(toSave);

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());

        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
        return new AuthResponse(newAccessToken, newRefreshTokenRaw, userResponse);
    }
}
