package com.example.modularmonolith.bootstrap;

import com.example.auth.domain.User;
import com.example.auth.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@example.com";
        String adminPassword = "Admin@123";

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> {
                    if (!"ROLE_ADMIN".equals(user.getRole())) {
                        user.setRole("ROLE_ADMIN");
                        userRepository.save(user);
                    }
                },
                () -> userRepository.save(new User(
                        adminEmail,
                        passwordEncoder.encode(adminPassword),
                        "System",
                        "Administrator",
                        "ROLE_ADMIN"
                ))
        );
    }
}
