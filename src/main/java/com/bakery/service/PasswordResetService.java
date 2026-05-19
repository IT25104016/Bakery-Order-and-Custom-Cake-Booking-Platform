package com.bakery.service;

import com.bakery.model.PasswordResetToken;
import com.bakery.model.User;
import com.bakery.repository.PasswordResetTokenRepository;
import com.bakery.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    private final UserRepository userRepository;

    private final JavaMailSender mailSender;

    private final PasswordEncoder passwordEncoder;

    private final String mailUsername;

    private final String mailPassword;

    public PasswordResetService(

            PasswordResetTokenRepository tokenRepository,

            UserRepository userRepository,

            JavaMailSender mailSender,

            PasswordEncoder passwordEncoder,

            @Value("${spring.mail.username:}")
            String mailUsername,

            @Value("${spring.mail.password:}")
            String mailPassword
    ) {

        this.tokenRepository = tokenRepository;

        this.userRepository = userRepository;

        this.mailSender = mailSender;

        this.passwordEncoder = passwordEncoder;

        this.mailUsername = mailUsername;

        this.mailPassword = mailPassword;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 1 - SEND RESET EMAIL
    // ─────────────────────────────────────────────────────────

    @Transactional
    public boolean sendResetEmail(
            String email,
            String baseUrl
    ) {

        Optional<User> userOpt =
                userRepository.findByEmail(email);

        // Security purpose
        if (userOpt.isEmpty()) {

            return true;
        }

        User user = userOpt.get();

        // Remove old tokens
        tokenRepository.findAll()
                .stream()

                        .filter(t -> t.getUser().getId() == user.getId())

                .forEach(tokenRepository::delete);

        // Generate token
        String token =
                UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        token,
                        user
                );

        tokenRepository.save(resetToken);

        // Create reset link
        String resetLink =
                baseUrl
                        + "/reset-password?token="
                        + token;

        // Send email
        sendEmail(
                user.getEmail(),
                user.getName(),
                resetLink
        );

        return true;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2 - VALIDATE TOKEN
    // ─────────────────────────────────────────────────────────

    public Optional<PasswordResetToken> validateToken(
            String token
    ) {

        return tokenRepository.findByToken(token)

                .filter(t -> !t.isExpired())

                .filter(t -> !t.isUsed());
    }

    // ─────────────────────────────────────────────────────────
    // STEP 3 - RESET PASSWORD
    // ─────────────────────────────────────────────────────────

    @Transactional
    public boolean resetPassword(
            String token,
            String newPassword
    ) {

        Optional<PasswordResetToken> tokenOpt =
                validateToken(token);

        if (tokenOpt.isEmpty()) {

            return false;
        }

        PasswordResetToken resetToken =
                tokenOpt.get();

        User user =
                resetToken.getUser();

        // Encode password
        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);

        return true;
    }

    // ─────────────────────────────────────────────────────────
    // SEND PROFESSIONAL HTML EMAIL
    // ─────────────────────────────────────────────────────────

    private void sendEmail(

            String toEmail,

            String name,

            String resetLink
    ) {

        try {

            // Console fallback
            if (mailUsername == null
                    || mailUsername.isBlank()
                    || mailPassword == null
                    || mailPassword.isBlank()) {

                System.out.println(
                        "\n================================="
                );

                System.out.println(
                        "PASSWORD RESET LINK"
                );

                System.out.println(
                        "================================="
                );

                System.out.println(
                        "User : " + toEmail
                );

                System.out.println(
                        "Link : " + resetLink
                );

                System.out.println(
                        "=================================\n"
                );

                return;
            }

            String subject =
                    "MC Bakers - Password Reset";

            String body =

                    "<div style='"
                            +       "font-family:Arial,sans-serif;"
                            +       "background:#f8f5f2;"
                            +       "padding:40px;"
                            +       "color:#333;"
                            +       "'>"

                            +       "<div style='"
                            +       "max-width:520px;"
                            +       "margin:auto;"
                            +       "background:white;"
                            +       "border-radius:14px;"
                            +       "overflow:hidden;"
                            +       "box-shadow:0 4px 18px rgba(0,0,0,.08);"
                            +       "'>"

                            +       "<div style='"
                            +       "background:#5C3B1E;"
                            +       "padding:28px;"
                            +       "text-align:center;"
                            +       "color:white;"
                            +       "'>"

                            +       "<img src='https://i.ibb.co/GQTQW1vD/MC-Bakers-logo-design.png' "
                            +       "width='90' "
                            +       "style='"
                            +       "border-radius:50%;"
                            +       "margin-bottom:14px;"
                            +       "border:3px solid #F0C878;"
                            +       "background:white;"
                            +       "padding:4px;"
                            +       "'>"

                            +       "<h1 style='margin:0;font-size:32px;'>"
                            +       "MC Bakers"
                            +       "</h1>"

                            +       "<p style='margin-top:8px;opacity:.9;font-size:14px;'>"
                            +       "Password Reset"
                            +       "</p>"

                            +       "</div>"

                            +       "<div style='padding:35px;'>"

                            +       "<p style='font-size:16px;margin-bottom:18px;'>"
                            +       "Hello " + name + ","
                            +       "</p>"

                            +       "<p style='font-size:15px;line-height:1.7;color:#555;'>"
                            +       "We received a request to reset your "
                            +       "<strong>MC Bakers</strong> account password."
                            +       "</p>"

                            +       "<p style='font-size:15px;line-height:1.7;color:#555;'>"
                            +       "Click the button below to reset your password:"
                            +       "</p>"

                            +       "<div style='text-align:center;margin:35px 0;'>"

                            +       "<a href='" + resetLink + "' "
                            +       "style='"
                            +       "background:#F0C878;"
                            +       "color:#4E342E;"
                            +       "text-decoration:none;"
                            +       "padding:16px 34px;"
                            +       "border-radius:12px;"
                            +       "font-size:18px;"
                            +       "font-weight:bold;"
                            +       "display:inline-block;"
                            +       "'>"

                            +       "Reset Password"

                            +       "</a>"

                            +       "</div>"

                            +       "<p style='font-size:14px;color:#777;line-height:1.8;'>"
                            +       "This reset link will expire in "
                            +       "<strong>30 minutes</strong>."
                            +       "</p>"

                            +       "<p style='font-size:14px;color:#777;line-height:1.8;'>"
                            +       "If you did not request this password reset, "
                            +       "please ignore this email."
                            +       "</p>"

                            +       "</div>"

                            +       "<div style='"
                            +       "background:#f3ece7;"
                            +       "padding:18px;"
                            +       "text-align:center;"
                            +       "font-size:13px;"
                            +       "color:#777;"
                            +       "'>"

                            +       "© 2026 MC Bakers. All Rights Reserved."

                            +       "</div>"

                            +       "</div>"

                            +       "</div>";

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true
                    );

            helper.setTo(toEmail);

            helper.setFrom(mailUsername);

            helper.setSubject(subject);

            helper.setText(body, true);

            mailSender.send(message);

            System.out.println(
                    "Password reset email sent."
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}