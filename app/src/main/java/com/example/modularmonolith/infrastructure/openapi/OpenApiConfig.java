package com.example.modularmonolith.infrastructure.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Modular Monolith API",
                description = "Authentication and order APIs for the modular monolith sample application.\n\nNote: The X-Device-Id header is required for login, refresh and logout endpoints for newly issued tokens. Legacy refresh tokens persisted before device binding were tagged with device_id equal to RefreshTokenConstants.LEGACY_DEVICE_ID (in code: com.example.auth.infrastructure.security.RefreshTokenConstants.LEGACY_DEVICE_ID) and are accepted from any device until they are rotated or revoked.",
                version = "1.0.0",
                contact = @Contact(name = "Example Team", email = "team@example.com")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
