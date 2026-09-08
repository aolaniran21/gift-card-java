package com.example.modularmonolith;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

@Testcontainers
public class TestcontainersPostgresSetup {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:15-alpine");

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("modular-monolith")
            .withUsername("test")
            .withPassword("test");

    private static boolean started = false;

    static {
        try {
            postgres.start();
            started = true;
        } catch (Throwable t) {
            // Docker/testcontainers not available in this environment. Fall back to existing test datasource (H2).
            System.err.println("[TestcontainersPostgresSetup] Could not start Postgres container: " + t.getMessage());
            started = false;
        }
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        if (!started) {
            // Do not override properties; leave default test datasource (H2) in place.
            return;
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        // Ensure Flyway uses the test datasource
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
