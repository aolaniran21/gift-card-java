package com.example.auth.application;

import com.example.auth.api.AuthResponse;
import com.example.auth.api.LoginRequest;
import com.example.auth.api.RegisterRequest;
import com.example.auth.api.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUserUseCase logoutUserUseCase;

    public AuthService(RegisterUserUseCase registerUserUseCase,
                       LoginUserUseCase loginUserUseCase,
                       RefreshTokenUseCase refreshTokenUseCase,
                       LogoutUserUseCase logoutUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
    }

    public UserResponse register(RegisterRequest request) {
        return registerUserUseCase.execute(request);
    }

    public AuthResponse login(LoginRequest request, String deviceId) {
        return loginUserUseCase.execute(request, deviceId);
    }

    public AuthResponse refresh(String refreshTokenValue, String deviceId) {
        return refreshTokenUseCase.execute(refreshTokenValue, deviceId);
    }

    public void logout(String refreshTokenValue, String deviceId) {
        logoutUserUseCase.execute(refreshTokenValue, deviceId);
    }
}
