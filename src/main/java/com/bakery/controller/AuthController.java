package com.bakery.controller;

import com.bakery.service.OtpService;
import com.bakery.service.ProductService;
import com.bakery.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final ProductService productService;

    public AuthController(UserService userService,
                          OtpService otpService,
                          ProductService productService) {

        this.userService = userService;
        this.otpService = otpService;
        this.productService = productService;
    }

    // ── Home Page ──────────────────────────────────────────────
    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute(
                "products",
                productService.getAvailableProducts()
        );

        return "customer/products";
    }

    // ── Login ──────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {

        if (error != null)
            model.addAttribute(
                    "error",
                    "Invalid email or password."
            );

        if (logout != null)
            model.addAttribute("logout", true);

        if (registered != null)
            model.addAttribute("registered", true);

        return "auth/login";
    }

    // ── Register Page ──────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage() {

        return "auth/register";
    }

    // ── Register Submit ────────────────────────────────────────
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes ra) {

        // Email already exists
        if (userService.existsByEmail(email)) {

            ra.addFlashAttribute(
                    "error",
                    "This email is already registered."
            );

            return "redirect:/register";
        }

        // Save temporary session data
        session.setAttribute("pending_name", name);

        session.setAttribute("pending_email", email);

        session.setAttribute("pending_password", password);

        // Send OTP
        try {

            otpService.sendOtp(email);

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "error",
                    "Sending OTP email failed."
            );

            return "redirect:/register";
        }

        ra.addFlashAttribute(
                "info",
                "OTP code sent to " + email
        );

        return "redirect:/verify-otp";
    }

    // ── Verify OTP Page ────────────────────────────────────────
    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session,
                                Model model) {

        String email =
                (String) session.getAttribute(
                        "pending_email"
                );

        if (email == null)
            return "redirect:/register";

        model.addAttribute("email", email);

        return "auth/verify-otp";
    }

    // ── Verify OTP Submit ──────────────────────────────────────
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            RedirectAttributes ra) {

        String email =
                (String) session.getAttribute(
                        "pending_email"
                );

        String name =
                (String) session.getAttribute(
                        "pending_name"
                );

        String password =
                (String) session.getAttribute(
                        "pending_password"
                );

        if (email == null)
            return "redirect:/register";

        // OTP validation
        if (!otpService.verifyOtp(email, otp.trim())) {

            ra.addFlashAttribute(
                    "error",
                    "OTP code is invalid or expired."
            );

            return "redirect:/verify-otp";
        }

        // Create user
        userService.registerCustomer(
                name,
                email,
                password
        );

        // Clear session
        session.removeAttribute("pending_name");
        session.removeAttribute("pending_email");
        session.removeAttribute("pending_password");

        return "redirect:/login?registered=true";
    }

    // ── Resend OTP ─────────────────────────────────────────────
    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session,
                            RedirectAttributes ra) {

        String email =
                (String) session.getAttribute(
                        "pending_email"
                );

        if (email == null)
            return "redirect:/register";

        try {

            otpService.sendOtp(email);

            ra.addFlashAttribute(
                    "info",
                    "OTP resent to " + email
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "error",
                    "OTP email sending failed."
            );
        }

        return "redirect:/verify-otp";
    }
}