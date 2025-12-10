package com.example.webserver.auth.dto;

// TokenInfo.java

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
    private String grantType; // "Bearer"
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn; // 만료 시각 (ms)
}