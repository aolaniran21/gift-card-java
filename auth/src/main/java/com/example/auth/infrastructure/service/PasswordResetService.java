package com.example.auth.infrastructure.service;

import com.example.auth.infrastructure.persistence.JpaPasswordResetTokenRepository;
import com.example.auth.infrastructure.persistence.PasswordResetTokenEntity;
import com.example.auth.infrastructure.persistence.JpaUserRepository;
import com.example.auth.infrastructure.persistence.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final JpaPasswordResetTokenRepository tokenRepository;
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(JpaPasswordResetTokenRepository tokenRepository,
                                JpaUserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String requestPasswordReset(String email) {
        // make sure user exists
        Optional<UserEntity> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            log.info("Password reset requested for non-existent email {}, ignoring for security", email);
            // return a safe message but do not reveal that email was not found
            return "ok";
        }

        String rawToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity(email, rawToken, expiresAt);
        tokenRepository.save(entity);

        // In production, send email. For now, log the reset token as a mock email
        log.info("Password reset token for {}: {} (expires at {})", email, rawToken, expiresAt);
        return rawToken;
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetTokenEntity entity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (entity.isUsed() || entity.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        UserEntity user = userRepository.findByEmail(entity.getUserEmail())
                .orElseThrow(() -> new IllegalStateException("User not found for password reset"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        entity.setUsed(true);
        tokenRepository.save(entity);

        log.info("Password reset completed for {}", user.getEmail());
    }
}
