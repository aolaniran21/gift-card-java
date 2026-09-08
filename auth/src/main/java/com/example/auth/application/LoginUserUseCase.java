package com.example.auth.application;

import com.example.auth.api.AuthResponse;
import com.example.auth.api.LoginRequest;
import com.example.auth.api.UserResponse;
import com.example.auth.domain.RefreshToken;
import com.example.auth.domain.RefreshTokenRepository;
import com.example.auth.domain.User;
import com.example.auth.domain.UserRepository;
import com.example.shared.JwtUtil;
import com.example.auth.infrastructure.security.RefreshTokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenGenerator refreshTokenGenerator;

    public LoginUserUseCase(UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil,
                            RefreshTokenGenerator refreshTokenGenerator) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenGenerator = refreshTokenGenerator;
    }

    @Transactional
    public AuthResponse execute(LoginRequest request, String deviceId) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        RefreshTokenGenerator.GeneratedRefreshToken generated = refreshTokenGenerator.generate(user.getEmail(), deviceId);
        String refreshTokenValue = generated.getRawToken();

        // persist tokenId (the domain object's token field holds tokenId for storage)
        refreshTokenRepository.deleteByEmail(user.getEmail());
        RefreshToken toSave = generated.getRefreshTokenDomain();
        refreshTokenRepository.save(toSave);

        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
        return new AuthResponse(accessToken, refreshTokenValue, userResponse);
    }
}
