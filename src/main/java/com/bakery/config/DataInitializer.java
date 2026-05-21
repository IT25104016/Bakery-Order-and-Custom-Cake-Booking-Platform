package com.bakery.config;

import com.bakery.model.*;
import com.bakery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;//Application start automatic initialization tasks run 
import org.springframework.jdbc.core.JdbcTemplate;////jdbc use for exectute direct SQL queries
import org.springframework.security.crypto.password.PasswordEncoder;//use for Password encrypt/hash 
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataInitializer implements CommandLineRunner {//(abstraction).
    //Dependency Injection Variables
    private final UserRepository userRepository;//encapsulation
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,//hashing password
                           JdbcTemplate jdbcTemplate) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate    = jdbcTemplate;//execute sql
    }

   @Override
public void run(String... args) {

    // Update database settings
    allowDeletedUsersInOrderHistory();
    allowUpdatedOrderStatuses();

    // Get default admin email and password
    String adminEmail = System.getenv().getOrDefault("DEFAULT_ADMIN_EMAIL", "admin@bakery.com");
    String adminPassword = System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "admin123");

    // Check if admin already exists
    if (!userRepository.existsByEmail(adminEmail)) {

        // Create new admin object
        AdminUser admin = new AdminUser();

        // Set admin details
        admin.setName("Administrator");
        admin.setEmail(adminEmail);

        // Encrypt password before saving
        admin.setPassword(passwordEncoder.encode(adminPassword));

        // Save admin to database
        userRepository.save(admin);

        // Print success message
        System.out.println("Default admin created: " + adminEmail);
    }
}

        List<User> users = userRepository.findAll();
        for (User user : users) {
            String pwd = user.getPassword();
            // BCrypt hashes start with $2a$ or $2b$
            if (pwd != null && !pwd.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                System.out.println(" Password migrated: " + user.getEmail());
            }

            // ── Polymorphism demo in logs ──────────────────────
            System.out.println(" " + user.getDisplayInfo()
                    + " → dashboard: " + user.getDashboardUrl());//“A method gives different outputs according to the object type.”

        }
    }
    //jdbc use for exectute direct SQL queries
    private void allowDeletedUsersInOrderHistory() {
        jdbcTemplate.execute("ALTER TABLE orders MODIFY COLUMN user_id INT NULL");
        jdbcTemplate.execute("ALTER TABLE custom_orders MODIFY COLUMN user_id INT NULL");
    }

    private void allowUpdatedOrderStatuses() {
        jdbcTemplate.execute("ALTER TABLE orders MODIFY COLUMN status VARCHAR(20) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE custom_orders MODIFY COLUMN status VARCHAR(20) NOT NULL");
    }
}
