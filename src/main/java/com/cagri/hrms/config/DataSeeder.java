package com.cagri.hrms.config;

import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.entity.core.Role;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.repository.LeaveDefinitionRepository;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;
    private final LeaveDefinitionRepository leaveDefinitionRepository;

    @Value("${admin.user.email}")
    private String adminEmail;

    @Value("${admin.user.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          RoleRepository roleRepository) {
        return args -> {
            // ---- Roles ----
            if (!roleRepository.existsByName("ADMIN")) {
                roleRepository.save(Role.builder().name("ADMIN").build());
            }
            if (!roleRepository.existsByName("EMPLOYEE")) {
                roleRepository.save(Role.builder().name("EMPLOYEE").build());
            }
            if (!roleRepository.existsByName("MANAGER")) {
                roleRepository.save(Role.builder().name("MANAGER").build());
            }

            // ---- Admin user ----
            if (!userRepository.existsByEmail(adminEmail))  {
                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
                User admin = User.builder()
                        .fullName("Site Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(adminRole)
                        .emailVerified(true)
                        .isActive(true)
                        .enabled(true)
                        .createdAt(LocalDate.now())
                        .build();
                userRepository.save(admin);
            }


            upsertLeaveDef("Annual Leave", true,  null, true); // Annual: from allocation
            upsertLeaveDef("Sick Leave",   false, null, true); // Sick: unlimited
        };
    }

    private void upsertLeaveDef(String name, boolean isAnnual, Integer maxDays, boolean active) {
        LeaveDefinition def = leaveDefinitionRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> LeaveDefinition.builder().name(name).build());

        boolean changed = false;

        if (def.isAnnual() != isAnnual) { def.setAnnual(isAnnual); changed = true; }
        if ((def.getMaxDays() == null && maxDays != null) ||
                (def.getMaxDays() != null && !def.getMaxDays().equals(maxDays))) {
            def.setMaxDays(maxDays); changed = true;
        }
        if (def.isActive() != active) { def.setActive(active); changed = true; }

        if (def.getId() == null || changed) {
            leaveDefinitionRepository.save(def);
        }
    }
}
