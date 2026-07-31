package com.cts.config;

import com.cts.entity.User;
import com.cts.enumeration.Role;
import com.cts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminInitializerConfig {

    private static final String DEFAULT_PASSWORD = "abc@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private record SeedUser(String name, String email, String phone, Role role,
                            String address, String city, String state, String country,
                            LocalDate dateOfBirth, String gender) {}

    @Bean
    public CommandLineRunner initializeDefaultUsers() {
        return args -> {
            List<SeedUser> seeds = List.of(
                new SeedUser("System Admin", "admin@gmail.com", "8000550100", Role.ADMIN,
                        "1 Operations Avenue", "Chennai", "Tamil Nadu", "India",
                        LocalDate.of(1985, 1, 15), "MALE"),
                new SeedUser("Finance Officer", "finance@gmail.com", "8000550200", Role.FINANCE_OFFICER,
                        "22 Ledger Street", "Mumbai", "Maharashtra", "India",
                        LocalDate.of(1988, 3, 22), "FEMALE"),
                new SeedUser("Compliance Officer", "compliance@gmail.com", "8000550300", Role.COMPLIANCE_OFFICER,
                        "8 Audit Road", "Bengaluru", "Karnataka", "India",
                        LocalDate.of(1986, 7, 9), "MALE"),
                new SeedUser("Default Travel Agent", "agent@gmail.com", "8000550500", Role.TRAVEL_AGENT,
                        "12 Voyager Court", "Pune", "Maharashtra", "India",
                        LocalDate.of(1990, 5, 5), "MALE")
            );

            for (SeedUser s : seeds) {
                if (userRepository.findByEmail(s.email()).isEmpty()) {
                    User user = User.builder()
                            .name(s.name())
                            .email(s.email())
                            .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                            .role(s.role())
                            .phone(s.phone())
                            .address(s.address())
                            .city(s.city())
                            .state(s.state())
                            .country(s.country())
                            .dateOfBirth(s.dateOfBirth())
                            .gender(s.gender())
                            .build();

                    if (s.role() == Role.TRAVEL_AGENT) {
                        user.setAgentBio("Experienced travel agent handling curated tour packages.");
                        user.setAgentExperienceYears(5);
                    }
                    userRepository.save(user);
                }
            }
        };
    }
}
