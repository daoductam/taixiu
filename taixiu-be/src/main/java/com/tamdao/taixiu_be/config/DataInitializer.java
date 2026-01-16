package com.tamdao.taixiu_be.config;

import com.tamdao.taixiu_be.entity.User;
import com.tamdao.taixiu_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@taixiu.com")
                    .password(passwordEncoder.encode("admin123"))
                    .balance(BigDecimal.valueOf(1000000))
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: username=admin, password=admin123");
        }
        
        // Create test user if not exists
        if (!userRepository.existsByUsername("player1")) {
            User testUser = User.builder()
                    .username("player1")
                    .email("player1@taixiu.com")
                    .password(passwordEncoder.encode("player123"))
                    .balance(BigDecimal.valueOf(100000))
                    .role(User.Role.USER)
                    .isActive(true)
                    .build();
            userRepository.save(testUser);
            log.info("Test user created: username=player1, password=player123");
        }
    }
}
