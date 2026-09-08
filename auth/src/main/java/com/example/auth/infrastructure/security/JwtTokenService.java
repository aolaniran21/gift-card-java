package com.example.auth.infrastructure.security;

import com.example.shared.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtUtil jwtUtil;

    public JwtTokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String createAccessToken(String subject, String role) {
        return jwtUtil.generateAccessToken(subject, role);
    }

    public String createRefreshToken(String subject, String role) {
        return jwtUtil.generateRefreshToken(subject, role);
    }
}
