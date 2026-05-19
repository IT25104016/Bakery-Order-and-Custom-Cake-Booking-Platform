package com.bakery.controller;

import com.bakery.model.PasswordResetToken;
import com.bakery.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // ── Step 1: Show "Forgot Password" form ───────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    // ── Step 2: Process email, send reset link ─────────────────
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        HttpServletRequest request,
                                        RedirectAttributes ra) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();
        try {
            passwordResetService.sendResetEmail(email, baseUrl);
            ra.addFlashAttribute("success",
                    "A password reset link has been sent to your email! Please check your inbox. (Valid for 30 minutes)\n");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "An error occurred while sending the email. Please try again.");
        }
        return "redirect:/forgot-password";
    }

    // ── Step 3: Show new password form (token from URL) ────────
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = passwordResetService.validateToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "This link has expired or is invalid. Please try again.");
            return "auth/reset-password";
        }
        model.addAttribute("token", token);
        model.addAttribute("userName", tokenOpt.get().getUser().getName());
        return "auth/reset-password";
    }

    // ── Step 4: Save new password ──────────────────────────────
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/reset-password?token=" + token;
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "The password must be at least 6 characters long.\n");
            return "redirect:/reset-password?token=" + token;
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (success) {
            ra.addFlashAttribute("passwordReset", "Your password has been successfully reset! Please log in.\n");
        } else {
            ra.addFlashAttribute("error", "The link has expired. Please try the “Forgot Password” process again.\n");
        }
        return "redirect:/login";
    }
}