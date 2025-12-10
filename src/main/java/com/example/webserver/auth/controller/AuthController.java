package com.example.webserver.auth.controller;

import com.example.webserver.auth.dto.LoginRequestDto;
import com.example.webserver.auth.dto.TokenDto;
import com.example.webserver.auth.dto.UserRequestDto;
import com.example.webserver.auth.dto.UserResponseDto;
import com.example.webserver.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // POST /api/v1/auth/signup : 회원가입 (permitAll() 설정)
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody @Valid UserRequestDto userRequestDto) {
        UserResponseDto response = authService.signup(userRequestDto);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/login : 로그인 (Access/Refresh Token 발급, permitAll() 설정)
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {
        TokenDto tokenDto = authService.login(loginRequest);
        return ResponseEntity.ok(tokenDto);
    }

    // POST /api/v1/auth/reissue : 토큰 재발급 (Refresh Token 사용, permitAll() 설정)
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto tokenRequestDto) {
        TokenDto tokenDto = authService.reissue(tokenRequestDto);
        return ResponseEntity.ok(tokenDto);
    }

    // POST /api/v1/auth/logout : 로그아웃 (본인 Refresh Token 삭제, authenticated() 설정)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessTokenHeader) {
        // Access Token에서 사용자 정보를 추출하여 로그아웃 처리
        String accessToken = accessTokenHeader.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // DELETE /api/v1/auth/deleteaccount : 계정 삭제/탈퇴 (본인 계정, authenticated() 설정)
    @DeleteMapping("/deleteaccount")
    public ResponseEntity<Void> deleteaccount(@RequestHeader("Authorization") String accessTokenHeader) {
        String accessToken = accessTokenHeader.substring(7);
        authService.deleteaccount(accessToken);
        return ResponseEntity.noContent().build();
    }
}