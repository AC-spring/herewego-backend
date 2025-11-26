package com.example.webserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    // 토큰의 인증 타입 (예: "Bearer")
    private String grantType;

    // 실제 API 접근에 사용되는 토큰 (만료 시간 짧음)
    private String accessToken;

    // 액세스 토큰 만료 시 재발급에 사용되는 토큰 (만료 시간 김)
    private String refreshToken;

    // (선택 사항) 액세스 토큰 만료 시간 (밀리초)
    private Long accessTokenExpiresIn;
}