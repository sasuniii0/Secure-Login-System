package com.example.secure_login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.otp.simulate:true}")
    private boolean simulate;

    @Value("${spring.mail.username:noreply@secureapp.com}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, int expiryMinutes) {
        if (simulate) {
            log.info("╔══════════════════════════════════════╗");
            log.info("║         OTP VERIFICATION CODE        ║");
            log.info("╠══════════════════════════════════════╣");
            log.info("║  To      : {}                        ", toEmail);
            log.info("║  OTP     : {}                        ", otp);
            log.info("║  Expires : In {} minutes              ", expiryMinutes);
            log.info("╚══════════════════════════════════════╝");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your SecureApp OTP Code");
            message.setText(buildEmailBody(otp, expiryMinutes));
            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed. Please try again.");
        }
    }

    private String buildEmailBody(String otp, int expiryMinutes) {
        return String.format("""
            Hello,
            
            Your One-Time Password (OTP) for SecureApp login is:
            
                 %s
            
            This code expires in %d minute(s). Do not share it with anyone.
            
            If you did not request this, please ignore this email.
            
            — SecureApp Security Team
            """, otp, expiryMinutes);
    }
}
