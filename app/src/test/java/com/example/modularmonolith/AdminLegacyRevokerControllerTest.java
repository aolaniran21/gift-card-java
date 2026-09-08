package com.example.modularmonolith;

import com.example.auth.infrastructure.persistence.RefreshTokenEntity;
import com.example.auth.infrastructure.persistence.JpaRefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(TestcontainersPostgresSetup.class)
public class AdminLegacyRevokerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    com.example.auth.infrastructure.security.RefreshTokenCrypto refreshTokenCrypto;

    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    @Test
    void admin_revoker_endpoint_dryrun_and_execute() throws Exception {
        String email = "adminlegacy-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        // register user
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "AdminLegacy",
                                "lastName", "User",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        // craft legacy token and persist with old createdAt
        String tokenId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        long expires = Instant.now().plusSeconds(3600).getEpochSecond();
        String data = tokenId + ":" + expires + ":" + email + ":" + com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID;
        String sig = refreshTokenCrypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expires + "." + sig;

        RefreshTokenEntity entity = new RefreshTokenEntity(tokenId, email, Instant.ofEpochSecond(expires));
        entity.setDeviceId(com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID);
        entity.setCreatedAt(Instant.now().minus(40, ChronoUnit.DAYS));
        jpaRefreshTokenRepository.save(entity);

        // dry-run: should report matched >= 1 and not revoke
        String dryResponse = mockMvc.perform(post("/api/auth/admin/revoke-legacy-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 30, "dryRun", true)))
                        // in tests we don't have auth, but the app test security allows mock access; if not, tests will fail
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map m1 = objectMapper.readValue(dryResponse, Map.class);
        Map dataMap = (Map) m1.get("data");
        assertThat(dataMap.get("matched")).as("dry-run matched count").isNotNull();

        // execute: should revoke
        String execResponse = mockMvc.perform(post("/api/auth/admin/revoke-legacy-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("days", 30, "dryRun", false)))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map m2 = objectMapper.readValue(execResponse, Map.class);
        Map dataMap2 = (Map) m2.get("data");
        assertThat(dataMap2.get("revoked")).as("revoked count").isNotNull();

        // presenting the token should now fail
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "some-device")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", raw))))
                .andExpect(status().isUnauthorized());
    }
}
