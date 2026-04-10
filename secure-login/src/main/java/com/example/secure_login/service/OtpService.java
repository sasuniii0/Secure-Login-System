package com.example.secure_login.service;

import com.example.secure_login.entity.OtpToken;
import com.example.secure_login.entity.User;
import com.example.secure_login.repository.OtpTokenRepository;
import com.example.secure_login.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Transactional
    public void generateAndSendOtp(User user) {
        otpTokenRepository.deleteAllByUser(user);

        String otpCode = otpGenerator.generate();

        OtpToken token = OtpToken.builder()
                .otpCode(otpCode)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        otpTokenRepository.save(token);

        emailService.sendOtpEmail(user.getEmail(), otpCode, expiryMinutes);
        log.debug("OTP generated for user: {}", user.getEmail());
    }

    @Transactional
    public boolean verifyOtp(User user, String otpCode) {
        return otpTokenRepository
                .findByUserAndOtpCodeAndUsedFalse(user, otpCode)
                .map(token -> {
                    if (token.isExpired()) {
                        log.warn("OTP expired for user: {}", user.getEmail());
                        return false;
                    }
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                    log.info("OTP verified for user: {}", user.getEmail());
                    return true;
                })
                .orElse(false);
    }

    @Scheduled(fixedRate = 600_000)
    @Transactional
    public void cleanupExpiredTokens() {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.debug("Cleaned up expired OTP tokens");
    }
}
