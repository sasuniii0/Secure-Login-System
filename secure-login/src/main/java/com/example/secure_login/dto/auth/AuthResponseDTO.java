package com.example.secure_login.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String message;
    private boolean success;
    private String accessToken;
    private String email;
    private String fullName;
}
