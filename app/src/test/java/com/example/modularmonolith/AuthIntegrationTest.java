package com.example.modularmonolith;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(TestcontainersPostgresSetup.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.auth.infrastructure.persistence.JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    private com.example.auth.infrastructure.security.RefreshTokenCrypto refreshTokenCrypto;

    @Autowired
    private com.example.auth.infrastructure.service.LegacyRefreshTokenRevoker legacyRefreshTokenRevoker;

    @Test
    void registerLoginAndAccessProtectedOrderEndpoint() throws Exception {
        String email = "alice-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        Map<String, Object> registerBody = Map.of(
                "firstName", "Alice",
                "lastName", "Smith",
                "email", email,
                "password", password
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        Map<String, Object> loginBody = Map.of(
                "email", email,
                "password", password
        );
        String deviceId = "device-" + UUID.randomUUID();

        String loginPayload = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> data = objectMapper.readValue(loginPayload, Map.class);
        Map<String, Object> authData = (Map<String, Object>) data.get("data");
        String accessToken = (String) authData.get("accessToken");

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanAccessAdminEndpointButUserCannot() throws Exception {
        String adminEmail = "admin@example.com";
        String adminPassword = "Admin@123";

        Map<String, Object> adminLoginBody = Map.of(
                "email", adminEmail,
                "password", adminPassword
        );

        String adminDevice = "device-admin";
        String adminLoginPayload = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", adminDevice)
                        .content(objectMapper.writeValueAsString(adminLoginBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> adminData = objectMapper.readValue(adminLoginPayload, Map.class);
        Map<String, Object> adminAuthData = (Map<String, Object>) adminData.get("data");
        String adminToken = (String) adminAuthData.get("accessToken");

        mockMvc.perform(get("/api/auth/admin/health")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        String userEmail = "bob-" + UUID.randomUUID() + "@example.com";
        String userPassword = "Pass@1234";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Bob",
                                "lastName", "Jones",
                                "email", userEmail,
                                "password", userPassword
                        ))))
                .andExpect(status().isCreated());

        String userDevice = "device-" + UUID.randomUUID();
        String userLoginPayload = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", userDevice)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", userEmail,
                                "password", userPassword
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> userData = objectMapper.readValue(userLoginPayload, Map.class);
        Map<String, Object> userAuthData = (Map<String, Object>) userData.get("data");
        String userToken = (String) userAuthData.get("accessToken");

        mockMvc.perform(get("/api/auth/admin/health")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshAndLogoutTokenLifecycleWorks() throws Exception {
        String email = "carol-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Carol",
                                "lastName", "Brown",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        String deviceId = "device-" + UUID.randomUUID();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> loginData = objectMapper.readValue(loginResponse, Map.class);
        Map<String, Object> authData = (Map<String, Object>) loginData.get("data");
        String refreshToken = (String) authData.get("refreshToken");

        String refreshedResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> refreshedData = objectMapper.readValue(refreshedResponse, Map.class);
        Map<String, Object> refreshedAuthData = (Map<String, Object>) refreshedData.get("data");
        String newRefreshToken = (String) refreshedAuthData.get("refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", newRefreshToken))))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReuseDetection_revokedOldTokenTriggersGlobalRevoke() throws Exception {
        String email = "dave-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Dave",
                                "lastName", "Lee",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        String deviceId = "device-" + UUID.randomUUID();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> loginData = objectMapper.readValue(loginResponse, Map.class);
        Map<String, Object> authData = (Map<String, Object>) loginData.get("data");
        String refreshToken1 = (String) authData.get("refreshToken");

        // use refreshToken1 to get a new token (rotation)
        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken1))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> refreshedData = objectMapper.readValue(refreshResponse, Map.class);
        Map<String, Object> refreshedAuthData = (Map<String, Object>) refreshedData.get("data");
        String refreshToken2 = (String) refreshedAuthData.get("refreshToken");

        // Attempt to reuse the old refreshToken1 -> should trigger reuse detection and return 401 Unauthorized
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken1))))
                .andExpect(status().isUnauthorized());

        // After reuse detection, all tokens for the user should be revoked. Even refreshToken2 should now be invalid (treated as unauthorized)
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceId)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken2))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void legacyTokenAcceptedForAnyDevice() throws Exception {
        String email = "legacy-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Legacy",
                                "lastName", "User",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        // craft a legacy-style refresh token and persist it with device_id equal to RefreshTokenConstants.LEGACY_DEVICE_ID (com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID)
        String tokenId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        long expires = java.time.Instant.now().plusSeconds(3600).getEpochSecond();
        // compute signature in the same way persisted tokens will be verified (include stored deviceId sentinel)
        String data = tokenId + ":" + expires + ":" + email + ":" + com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID;
        String sig = refreshTokenCrypto.computeSignatureBase64Url(data);
        String raw = tokenId + "." + expires + "." + sig;

        com.example.auth.infrastructure.persistence.RefreshTokenEntity entity = new com.example.auth.infrastructure.persistence.RefreshTokenEntity(tokenId, email, java.time.Instant.ofEpochSecond(expires));
        entity.setDeviceId(com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID);
        jpaRefreshTokenRepository.save(entity);

        // Present the legacy token with an arbitrary device header - adapter should accept it
        String presentedDevice = "some-device-" + UUID.randomUUID();
        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", presentedDevice)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", raw))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> refreshedData = objectMapper.readValue(refreshResponse, Map.class);
        Map<String, Object> refreshedAuthData = (Map<String, Object>) refreshedData.get("data");
        // should receive a new refresh token (device-bound) in response
        org.junit.jupiter.api.Assertions.assertNotNull(refreshedAuthData.get("refreshToken"));
    }

    @Test
    void newTokenRequiresSameDevice() throws Exception {
        String email = "newtok-" + UUID.randomUUID() + "@example.com";
        String password = "Pass@1234";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "New",
                                "lastName", "Token",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isCreated());

        String deviceA = "device-A-" + UUID.randomUUID();
        String deviceB = "device-B-" + UUID.randomUUID();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceA)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> loginData = objectMapper.readValue(loginResponse, Map.class);
        Map<String, Object> authData = (Map<String, Object>) loginData.get("data");
        String refreshToken = (String) authData.get("refreshToken");

        // Attempt to use the token from deviceA while presenting deviceB -> should be rejected
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", deviceB)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }
}
