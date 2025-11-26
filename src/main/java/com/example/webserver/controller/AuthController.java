package com.example.webserver.controller;

import com.example.webserver.dto.LoginRequestDto;
import com.example.webserver.dto.TokenDto; // 💡 TokenDto 임포트 추가
import com.example.webserver.dto.UserRequestDto;
import com.example.webserver.dto.UserResponseDto;
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

    // -----------------------------------------------------------------
    // 🔑 POST /api/v1/auth/login 요청 처리 (TokenDto 반환)
    // -----------------------------------------------------------------
    @PostMapping("/login")
    // 🚨 반환 타입을 String에서 TokenDto로 변경
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginRequestDto loginRequest) {

        // 💡 TokenDto 반환
        TokenDto tokenDto = authService.login(loginRequest);

        // TokenDto를 응답 본문에 담아 클라이언트에게 전달
        return ResponseEntity.ok(tokenDto);
    }
}