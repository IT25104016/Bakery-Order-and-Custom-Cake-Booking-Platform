package com.bakery.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final JavaMailSender mailSender;

    private final String mailUsername;

    private final String mailPassword;

    // email -> otp + expiry
    private final Map<String, OtpEntry> otpStore =
            new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    public OtpService(
            JavaMailSender mailSender,

            @Value("${spring.mail.username:}")
            String mailUsername,

            @Value("${spring.mail.password:}")
            String mailPassword
    ) {

        this.mailSender = mailSender;

        this.mailUsername = mailUsername;

        this.mailPassword = mailPassword;
    }

    // ── Generate & Send OTP ────────────────────────────────────
    public void sendOtp(String email) {

        String otp = generateOtp();

        otpStore.put(

                email,

                new OtpEntry(
                        otp,
                        LocalDateTime.now()
                                .plusMinutes(
                                        OTP_EXPIRY_MINUTES
                                )
                )
        );

        sendEmail(email, otp);
    }

    // ── Verify OTP ─────────────────────────────────────────────
    public boolean verifyOtp(
            String email,
            String enteredOtp
    ) {

        OtpEntry entry =
                otpStore.get(email);

        if (entry == null)
            return false;

        // Expired
        if (LocalDateTime.now()
                .isAfter(entry.expiry())) {

            otpStore.remove(email);

            return false;
        }

        // Correct OTP
        if (entry.otp().equals(enteredOtp)) {

            otpStore.remove(email);

            return true;
        }

        return false;
    }

    // ── Check Pending OTP ──────────────────────────────────────
    public boolean hasPendingOtp(String email) {

        OtpEntry entry =
                otpStore.get(email);

        return entry != null
                && LocalDateTime.now()
                .isBefore(entry.expiry());
    }

    // ── Generate 6 Digit OTP ───────────────────────────────────
    private String generateOtp() {

        return String.format(
                "%06d",
                new Random().nextInt(1_000_000)
        );
    }

    // ── Send Professional HTML Email ───────────────────────────
    private void sendEmail(
            String to,
            String otp
    ) {

        try {

            // Console fallback
            if (mailUsername == null
                    || mailUsername.isBlank()
                    || mailPassword == null
                    || mailPassword.isBlank()) {

                System.out.println(
                        "OTP for "
                                + to
                                + ": "
                                + otp
                );

                return;
            }

            String subject =
                    "MC Bakers - Email Verification";

            String body = """

                <div style="
                    font-family:Arial,sans-serif;
                    background:#f8f5f2;
                    padding:40px;
                    color:#333;
                ">

                    <div style="
                        max-width:520px;
                        margin:auto;
                        background:white;
                        border-radius:14px;
                        overflow:hidden;
                        box-shadow:0 4px 18px rgba(0,0,0,.08);
                    ">

                        <!-- Header -->
                        <div style="
                            background:#5C3B1E;
                            padding:28px;
                            text-align:center;
                            color:white;
                        ">
                             <img src="https://i.ibb.co/GQTQW1vD/MC-Bakers-logo-design.png"
                                         alt="MC Bakers Logo"
                                         width="90"
                                         style="
                                            border-radius:50%;
                                            margin-bottom:14px;
                                            border:3px solid #F0C878;
                                            background:white;
                                            padding:4px;
                                         ">
                            <h1 style="
                                margin:0;
                                font-size:32px;
                            ">
                                MC Bakers
                            </h1>

                            <p style="
                                margin-top:8px;
                                opacity:.9;
                                font-size:14px;
                            ">
                                Email Verification
                            </p>

                        </div>

                        <!-- Content -->
                        <div style="padding:35px;">

                            <p style="
                                font-size:16px;
                                margin-bottom:18px;
                            ">
                                Dear Customer,
                            </p>

                            <p style="
                                font-size:15px;
                                line-height:1.7;
                                color:#555;
                            ">
                                Thank you for choosing
                                <strong>MC Bakers</strong>.
                            </p>

                            <p style="
                                font-size:15px;
                                line-height:1.7;
                                color:#555;
                            ">
                                Please use the following OTP
                                code to verify your account:
                            </p>

                            <!-- OTP Box -->
                            <div style="
                                text-align:center;
                                margin:35px 0;
                            ">

                                <span style="
                                    display:inline-block;
                                    background:#F0C878;
                                    color:#4E342E;
                                    font-size:34px;
                                    font-weight:bold;
                                    letter-spacing:10px;
                                    padding:18px 34px;
                                    border-radius:14px;
                                ">

                                    """ + otp + """

                                </span>

                            </div>

                            <p style="
                                font-size:14px;
                                color:#777;
                                line-height:1.8;
                            ">
                                This OTP will expire in
                                <strong>
                                    """ + OTP_EXPIRY_MINUTES + """
                                    minutes
                                </strong>.
                            </p>

                            <p style="
                                font-size:14px;
                                color:#777;
                                line-height:1.8;
                            ">
                                If you did not request this
                                verification, please ignore
                                this email.
                            </p>

                        </div>

                        <!-- Footer -->
                        <div style="
                            background:#f3ece7;
                            padding:18px;
                            text-align:center;
                            font-size:13px;
                            color:#777;
                        ">

                            © 2026 MC Bakers.
                            All Rights Reserved.

                        </div>

                    </div>

                </div>

                """;

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true
                    );

            helper.setTo(to);

            helper.setSubject(subject);

            helper.setText(body, true);

            mailSender.send(message);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ── Inner Record ───────────────────────────────────────────
    private record OtpEntry(
            String otp,
            LocalDateTime expiry
    ) {}
}