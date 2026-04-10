package com.example.secure_login.service;

import com.example.secure_login.dto.auth.*;
import com.example.secure_login.entity.User;
import com.example.secure_login.repository.UserRepository;
import com.example.secure_login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @Transactional
    public MessageResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", request.getEmail());

        return new MessageResponseDTO("Registration successful. Please log in.", true);
    }

    @Transactional
    public MessageResponseDTO initiateLogin(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        otpService.generateAndSendOtp(user);
        log.info("OTP sent to: {}", user.getEmail());

        return new MessageResponseDTO(
                "Password verified. OTP sent to " + maskEmail(user.getEmail()),
                true
        );
    }

    @Transactional
    public AuthResponseDTO verifyOtpAndLogin(OtpVerifyRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        boolean valid = otpService.verifyOtp(user, request.getOtp());
        if (!valid) {
            throw new BadCredentialsException("Invalid or expired OTP");
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        log.info("Login complete for: {}", user.getEmail());

        return AuthResponseDTO.builder()
                .message("Login successful!")
                .success(true)
                .accessToken(jwt)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.charAt(0) + "*".repeat(at - 2) + email.substring(at - 1);
    }
}
