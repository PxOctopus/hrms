package com.cagri.hrms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // Create admin role if not exists
            if (roleRepository.count() == 0) {
                Role adminRole = Role.builder()
                        .name(UserRole.ADMIN)
                        .build();
                roleRepository.save(adminRole);
            }

            // Create admin user if not exists
            if (userRepository.count() == 0) {
                Role adminRole = roleRepository.findByName(UserRole.ADMIN).orElseThrow();

                User admin = User.builder()
                        .fullName("Site Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123")) // Use environment variable in real projects
                        .role(adminRole)
                        .userStatus(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .isActive(true)
                        .createdAt(LocalDate.now())
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
