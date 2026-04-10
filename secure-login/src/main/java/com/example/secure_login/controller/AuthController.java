package com.example.secure_login.controller;


import com.example.secure_login.dto.auth.*;
import com.example.secure_login.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * STEP 1 — Register a new user account.
     *
     * POST /api/auth/register
     * Body: { "fullName": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * STEP 2 — Login with email + password.
     * On success, an OTP is sent to the user's email.
     *
     * POST /api/auth/login
     * Body: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.initiateLogin(request));
    }

    /**
     * STEP 3 — Verify OTP to complete login and receive JWT.
     *
     * POST /api/auth/verify-otp
     * Body: { "email": "...", "otp": "123456" }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(
            @Valid @RequestBody OtpVerifyRequestDTO request) {
        return ResponseEntity.ok(authService.verifyOtpAndLogin(request));
    }
}
