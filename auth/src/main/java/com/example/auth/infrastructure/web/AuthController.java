package com.example.auth.infrastructure.web;

import com.example.auth.api.*;
import com.example.auth.application.AuthService;
import com.example.shared.ApiResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.example.auth.infrastructure.service.PasswordResetService passwordResetService;
    private final com.example.auth.infrastructure.service.LegacyRefreshTokenRevoker legacyRevoker;

    public AuthController(AuthService authService, com.example.auth.infrastructure.service.PasswordResetService passwordResetService, com.example.auth.infrastructure.service.LegacyRefreshTokenRevoker legacyRevoker) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.legacyRevoker = legacyRevoker;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           @Parameter(name = "X-Device-Id", in = ParameterIn.HEADER, description = "Device identifier (required for new tokens). Legacy tokens persisted before device binding were tagged with device_id equal to RefreshTokenConstants.LEGACY_DEVICE_ID (com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID) and are accepted from any device until they are rotated or revoked.", required = true)
                                                           @RequestHeader(value = "X-Device-Id", required = true) String deviceId) {
        AuthResponse response = authService.login(request, deviceId);
        return ResponseEntity.ok(new ApiResponse<>("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                              @Parameter(name = "X-Device-Id", in = ParameterIn.HEADER, description = "Device identifier (required for new tokens). Legacy tokens persisted before device binding were tagged with device_id equal to RefreshTokenConstants.LEGACY_DEVICE_ID (com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID) and are accepted from any device until they are rotated or revoked.", required = true)
                                                              @RequestHeader(value = "X-Device-Id", required = true) String deviceId) {
        AuthResponse response = authService.refresh(request.refreshToken(), deviceId);
        return ResponseEntity.ok(new ApiResponse<>("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request,
                                                    @Parameter(name = "X-Device-Id", in = ParameterIn.HEADER, description = "Device identifier (required for new tokens). Legacy tokens persisted before device binding were tagged with device_id equal to RefreshTokenConstants.LEGACY_DEVICE_ID (com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID) and are accepted from any device until they are rotated or revoked.", required = true)
                                                    @RequestHeader(value = "X-Device-Id", required = true) String deviceId) {
        authService.logout(request.refreshToken(), deviceId);
        return ResponseEntity.ok(new ApiResponse<>("Logged out successfully", null));
    }

    @GetMapping("/admin/health")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminHealth() {
        return ResponseEntity.ok(new ApiResponse<>("Admin access confirmed", "ok"));
    }

    @PostMapping("/admin/revoke-legacy-tokens")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> revokeLegacyTokens(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.example.auth.api.LegacyRevokerRequest request) {
        int days = request.days();
        boolean dryRun = request.dryRun();
        if (dryRun) {
            int matched = legacyRevoker.countLegacyTokensOlderThanDays(days);
            return ResponseEntity.ok(new ApiResponse<>("Legacy revoker dry-run completed", java.util.Map.of("matched", matched)));
        } else {
            int revoked = legacyRevoker.revokeLegacyTokensOlderThanDays(days);
            return ResponseEntity.ok(new ApiResponse<>("Legacy revoker executed", java.util.Map.of("revoked", revoked)));
        }
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@Valid @RequestBody com.example.auth.api.PasswordResetRequest request) {
        String token = passwordResetService.requestPasswordReset(request.getEmail());
        // In tests we may want the token, but in production this would be sent by email
        return ResponseEntity.ok(new ApiResponse<>("Password reset requested", token));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody com.example.auth.api.ConfirmPasswordResetRequest request) {
        passwordResetService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>("Password has been reset", null));
    }
}

