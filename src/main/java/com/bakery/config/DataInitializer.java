package com.bakery.config;

import com.bakery.model.*;
import com.bakery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate    = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        allowDeletedUsersInOrderHistory();

        String adminEmail = System.getenv().getOrDefault("DEFAULT_ADMIN_EMAIL", "admin@bakery.com");
        String adminPassword = System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "admin123");
        if (!userRepository.existsByEmail(adminEmail)) {
            AdminUser admin = new AdminUser();
            admin.setName("Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            System.out.println("Default admin created: " + adminEmail);
        }

        List<User> users = userRepository.findAll();
        for (User user : users) {
            String pwd = user.getPassword();
            // BCrypt hashes start with $2a$ or $2b$
            if (pwd != null && !pwd.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                System.out.println("✅ Password migrated: " + user.getEmail());
            }

            // ── Polymorphism demo in logs ──────────────────────
            System.out.println("🔑 " + user.getDisplayInfo()
                    + " → dashboard: " + user.getDashboardUrl());
        }
    }

    private void allowDeletedUsersInOrderHistory() {
        jdbcTemplate.execute("ALTER TABLE orders MODIFY COLUMN user_id INT NULL");
        jdbcTemplate.execute("ALTER TABLE custom_orders MODIFY COLUMN user_id INT NULL");
    }
}
