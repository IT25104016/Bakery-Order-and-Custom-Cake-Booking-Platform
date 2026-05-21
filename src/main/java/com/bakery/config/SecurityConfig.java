package com.bakery.config;

import com.bakery.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;//Used to configure authentication settings.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;//Used to configure HTTP security rules.
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;//This class indicates that it is a configuration class.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;//Imports BCrypt password hashing algorithm.
import org.springframework.security.crypto.password.PasswordEncoder;//Imports the PasswordEncoder interface.
import org.springframework.security.web.SecurityFilterChain;//Used to configure the security filter chain.

@Configuration//Marks this class as a Spring configuration class.
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;// Service object used for user authentication details
    public SecurityConfig(
            CustomUserDetailsService userDetailsService
    ) {

        this.userDetailsService = userDetailsService;
    }

    // ── Password Encoder ───────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();//Passwords are securely hashed before saving to the database.
    }

    // ── Authentication Manager ─────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    // ── Security Filter Chain ──────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http

                // ── Authorization ─────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Public Pages
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify-otp",
                                "/resend-otp",
                                "/forgot-password",
                                "/reset-password",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Customer Pages
                        .requestMatchers("/customer/**")
                        .permitAll()

                        // Admin Pages
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // Any Other Request
                        .anyRequest()
                        .authenticated()
                )

                // ── Login Configuration ───────────────────────────
                .formLogin(form -> form

                        .loginPage("/login")

                        .loginProcessingUrl("/login")

                        .usernameParameter("email")

                        .passwordParameter("password")

                        .successHandler((request,
                                         response,
                                         authentication) -> {

                            var authorities =
                                    authentication.getAuthorities();

                            String role =
                                    authorities.iterator()
                                            .next()
                                            .getAuthority();

                            // Admin Redirect
                            if (role.equals("ROLE_ADMIN")) {

                                response.sendRedirect(
                                        "/admin/dashboard"
                                );

                            } else {

                                // Customer Redirect
                                response.sendRedirect(
                                        "/customer/products"
                                );
                            }
                        })

                        .failureUrl("/login?error=true")

                        .permitAll()
                )

                // ── Logout ────────────────────────────────────────
                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl(
                                "/login?logout=true"
                        )

                        .invalidateHttpSession(true)

                        .permitAll()
                )

                // ── Disable CSRF ──────────────────────────────────
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
