package com.cagri.hrms.config;

import com.cagri.hrms.entity.Role;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    @Value("${admin.user.email}")
    private String adminEmail;

    @Value("${admin.user.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // Create an admin role if not exists
            if (roleRepository.count() == 0) {
                Role adminRole = Role.builder()
                        .name("ADMIN")
                        .build();
                roleRepository.save(adminRole);
            }

            // Create an EMPLOYEE role if it doesn't exist
            if (!roleRepository.existsByName("EMPLOYEE")) {
                Role employeeRole = Role.builder()
                        .name("EMPLOYEE")
                        .build();
                roleRepository.save(employeeRole);
            }

            // Create an admin user if not exists
            if (userRepository.count() == 0) {
                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

                User admin = User.builder()
                        .fullName("Site Admin")
                        .email(adminEmail) // Retrieved from environment variable
                        .password(passwordEncoder.encode(adminPassword)) // Retrieved from environment variable
                        .role(adminRole)
                        .emailVerified(true)
                        .isActive(true)
                        .createdAt(LocalDate.now())
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
