package com.example.webserver.controller;

import com.example.webserver.dto.request.LoginRequestDto;
import com.example.webserver.dto.TokenDto;
import com.example.webserver.dto.request.UserRequestDto;
import com.example.webserver.dto.response.UserResponseDto;
import com.example.webserver.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // POST /api/v1/auth/signup 요청 처리
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody @Valid UserRequestDto userRequestDto) {
        UserResponseDto response = authService.signup(userRequestDto);
        return ResponseEntity.ok(response);
    }

    // 🔑 POST /api/v1/auth/login 요청 처리 (Access/Refresh Token 발급)
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
        TokenDto tokenDto = authService.login(loginRequest);
        return ResponseEntity.ok(tokenDto);
    }

    // -----------------------------------------------------------------
    // ✨ POST /api/v1/auth/reissue 요청 처리 (토큰 재발급)
    // -----------------------------------------------------------------
    /**
     * Access Token이 만료되었을 때, Refresh Token을 사용하여 재발급을 요청합니다.
     */
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto tokenRequestDto) {
        // TokenRequestDto에는 만료되지 않은 Refresh Token이 포함되어야 합니다.
        TokenDto tokenDto = authService.reissue(tokenRequestDto);
        return ResponseEntity.ok(tokenDto);
    }

    // -----------------------------------------------------------------
    // ✨ POST /api/v1/auth/logout 요청 처리 (Refresh Token 삭제)
    // -----------------------------------------------------------------
    /**
     * 로그아웃 시 서버의 DB에서 Refresh Token을 삭제하여 세션을 무효화합니다.
     * 클라이언트에서는 Authorization 헤더의 Access Token으로 사용자를 식별합니다.
     */
    @PostMapping("/logout")
    // 인증된 사용자 정보를 얻기 위해 @RequestHeader("Authorization") 또는 Spring Security Context 사용
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessTokenHeader) {

        // 헤더에서 "Bearer " 부분을 제거하고 실제 Access Token만 추출
        String accessToken = accessTokenHeader.substring(7);

        authService.logout(accessToken);

        // 204 No Content 응답은 클라이언트에게 성공적으로 처리되었음을 알립니다.
        return ResponseEntity.noContent().build();
    }
}