package com.example.modularmonolith;

import com.example.auth.infrastructure.persistence.RefreshTokenEntity;
import com.example.auth.infrastructure.persistence.JpaRefreshTokenRepository;
import com.example.auth.infrastructure.service.LegacyRefreshTokenRevoker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(TestcontainersPostgresSetup.class)
public class LegacyRevokerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    com.example.auth.infrastructure.security.RefreshTokenCrypto refreshTokenCrypto;

    @Autowired
    LegacyRefreshTokenRevoker legacyRefreshTokenRevoker;

    @Test
    void revoker_revokes_legacy_token_and_refresh_fails() throws Exception {
        String email = "legacy2-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Legacy2",
                                "lastName", "User",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        // craft legacy token and persist
        String tokenId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        long expires = Instant.now().plusSeconds(3600).getEpochSecond();
        String data = tokenId + ":" + expires + ":" + email + ":" + com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID;
        String sig = refreshTokenCrypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expires + "." + sig;

        RefreshTokenEntity entity = new RefreshTokenEntity(tokenId, email, Instant.ofEpochSecond(expires));
        entity.setDeviceId(com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID);
        jpaRefreshTokenRepository.save(entity);

        // ensure it's present and not revoked
        RefreshTokenEntity saved = jpaRefreshTokenRepository.findByToken(tokenId).orElseThrow();
        assertThat(saved.getRevokedAt()).isNull();

        // run revoker
        int revoked = legacyRefreshTokenRevoker.revokeAllLegacyTokens();
        assertThat(revoked).isGreaterThanOrEqualTo(1);

        RefreshTokenEntity after = jpaRefreshTokenRepository.findByToken(tokenId).orElseThrow();
        assertThat(after.getRevokedAt()).isNotNull();

        // presenting the same legacy token should now fail
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "some-device")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", raw))))
                .andExpect(status().isUnauthorized());
    }
}
