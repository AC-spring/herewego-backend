package com.example.webserver.controller;

import com.example.webserver.dto.UserRequestDto;
import com.example.webserver.dto.UserResponseDto;
import com.example.webserver.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth") // <<< /api/v1/ 경로 추가
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // POST /api/v1/auth/signup 요청 처리 (올바른 DTO 사용)
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody @Valid UserRequestDto userRequestDto) {
        UserResponseDto response = authService.signup(userRequestDto);
        return ResponseEntity.ok(response);
    }
}