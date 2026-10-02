package com.shri.expense_tracker.config;

import com.shri.expense_tracker.model.User;
import com.shri.expense_tracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository) {
        return args -> {
            User testUser = userRepository.findByEmail("test@example.com")
                    .orElseGet(() -> new User("Test User", "test@example.com"));

            if (testUser.getMonthlyBudget() == null) {
                testUser.setMonthlyBudget(new BigDecimal("20000"));
            }

            userRepository.save(testUser);
        };
    }
}