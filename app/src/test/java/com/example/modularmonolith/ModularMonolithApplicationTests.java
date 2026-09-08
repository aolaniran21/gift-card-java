package com.example.modularmonolith;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.context.annotation.Import(TestcontainersPostgresSetup.class)
class ModularMonolithApplicationTests {

    @Test
    void contextLoads() {
    }
}
