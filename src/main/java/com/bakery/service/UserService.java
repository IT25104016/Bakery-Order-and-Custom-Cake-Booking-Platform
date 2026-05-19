package com.bakery.service;

import com.bakery.model.*;
import com.bakery.repository.CustomOrderRepository;
import com.bakery.repository.OrderRepository;
import com.bakery.repository.PasswordResetTokenRepository;
import com.bakery.repository.ProductRatingRepository;
import com.bakery.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    public static final int MAIN_ADMIN_ID = 1;
    public static final String MAIN_ADMIN_EMAIL = "admin@bakery.com";

    private final UserRepository userRepository;
    private final ProductRatingRepository productRatingRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final OrderRepository orderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageUploadService imageUploadService;

    public UserService(UserRepository userRepository,
                       ProductRatingRepository productRatingRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       OrderRepository orderRepository,
                       CustomOrderRepository customOrderRepository,
                       PasswordEncoder passwordEncoder,
                       ImageUploadService imageUploadService) {
        this.userRepository                 = userRepository;
        this.productRatingRepository        = productRatingRepository;
        this.passwordResetTokenRepository   = passwordResetTokenRepository;
        this.orderRepository                = orderRepository;
        this.customOrderRepository          = customOrderRepository;
        this.passwordEncoder                = passwordEncoder;
        this.imageUploadService             = imageUploadService;
    }

    public CustomerUser registerCustomer(String name, String email, String password) {
        CustomerUser user = new CustomerUser();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return (CustomerUser) userRepository.save(user);
    }


    public AdminUser createAdmin(
            String name,
            String email,
            String password
    ) {

        AdminUser admin =
                new AdminUser();

        admin.setName(name);

        admin.setEmail(email);

        admin.setPassword(
                passwordEncoder.encode(password)
        );

        return (AdminUser)
                userRepository.save(admin);
    }





    // ── Update profile (name + password) ──────────────────────
    public User updateProfile(int id, String name, String newPassword) {
        User user = userRepository.findById(id).orElseThrow();
        user.setName(name);
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        return userRepository.save(user);
    }

    // ── Update profile picture ─────────────────────────────────
    public User updateProfilePic(int id, org.springframework.web.multipart.MultipartFile file) throws Exception {
        User user = userRepository.findById(id).orElseThrow();
        String filename = imageUploadService.uploadProfileImage(file);
        if (filename != null) user.setProfilePic(filename);
        return userRepository.save(user);
    }

    // ── Delete profile picture ─────────────────────────────────
    public void deleteProfilePic(int id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setProfilePic(null);
        userRepository.save(user);
    }

    public String getDashboardUrlForUser(User user)    { return user.getDashboardUrl(); }
    public String getWelcomeMessageForUser(User user)  { return user.getWelcomeMessage(); }

    public List<User> getAllUsers()                    { return userRepository.findAll(); }
    public Optional<User> findByEmail(String email)   { return userRepository.findByEmail(email); }
    public Optional<User> findById(int id)            { return userRepository.findById(id); }
    public boolean existsByEmail(String email)        { return userRepository.existsByEmail(email); }

    public boolean isMainAdmin(User user) {
        return user != null
                && user.getId() == MAIN_ADMIN_ID
                && MAIN_ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())
                && "ADMIN".equals(user.getRole());
    }

    public boolean canDeleteUser(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }

        if (currentUser.getId() == targetUser.getId()) {
            return false;
        }

        if (!"ADMIN".equals(currentUser.getRole())) {
            return false;
        }

        if (!"ADMIN".equals(targetUser.getRole())) {
            return true;
        }

        return isMainAdmin(currentUser) && !isMainAdmin(targetUser);
    }

    @Transactional
    public void deleteUser(int id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow();

        productRatingRepository.deleteByUserId(id);
        passwordResetTokenRepository.deleteByUserId(id);
        customOrderRepository.clearUserByUserId(id);
        orderRepository.clearUserByUserId(id);
        customOrderRepository.flush();
        orderRepository.flush();
        userRepository.delete(user);
    }

    public long countCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof CustomerUser).count();
    }


}
