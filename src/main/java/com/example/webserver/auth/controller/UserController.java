package com.example.webserver.auth.controller;

import com.example.webserver.auth.dto.NicknameUpdateRequestDto;
import com.example.webserver.auth.dto.PasswordUpdateRequestDto;
import com.example.webserver.auth.dto.UserResponseDto;
import com.example.webserver.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- 마이페이지 기능 (Authenticated Required) ---

    // GET /api/v1/user/me : 현재 로그인된 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        // 토큰에서 추출된 사용자 ID 사용
        String loginUserId = userDetails.getUsername();
        UserResponseDto myInfo = userService.findUserByLoginId(loginUserId);
        return ResponseEntity.ok(myInfo);
    }

    // PUT /api/v1/user/nickname : 닉네임 변경
    @PutMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid NicknameUpdateRequestDto request
    ) {
        String loginUserId = userDetails.getUsername();
        userService.updateNickname(loginUserId, request.getNewNickname());
        return ResponseEntity.noContent().build();
    }

    // PUT /api/v1/user/password : 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PasswordUpdateRequestDto request
    ) {
        String loginUserId = userDetails.getUsername();
        userService.updatePassword(loginUserId,
                request.getCurrentPassword(),
                request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // --- 관리자 기능 (ROLE_ADMIN Required) ---


}