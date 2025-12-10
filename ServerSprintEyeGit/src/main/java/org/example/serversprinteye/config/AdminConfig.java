package org.example.serversprinteye.config;


import lombok.RequiredArgsConstructor;
import org.example.serversprinteye.domain.Role;
import org.example.serversprinteye.domain.User;
import org.example.serversprinteye.repositories.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AdminConfig {
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @Bean
    public CommandLineRunner makeAdmin() {
        return args -> {
            Optional<User> existingAdmin = userRepo.findByUserEmail("admin@serversprinteye.pl");

            if (existingAdmin.isEmpty()) {
                User admin = new User()
                        .setUserEmail("admin@serversprinteye.pl")
                        .setUserName("admin")
                        .setPassword(passwordEncoder.encode("admin123"))
                        .setEmailVerified(true)
                        .setRole(Role.ADMIN);

                userRepo.save(admin);
                System.out.println("###############");
                System.out.println("Admin created!");
                System.out.println("###############");
            }
        };
    }
}
