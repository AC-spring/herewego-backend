package com.example.webserver.controller;

import com.example.webserver.dto.request.NicknameUpdateRequestDto;
import com.example.webserver.dto.request.PasswordUpdateRequestDto;
import com.example.webserver.dto.response.UserResponseDto;
import com.example.webserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- 마이페이지 기능 ---

    /**
     * [GET] /api/v1/user/me : 현재 로그인된 사용자의 상세 정보를 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String loginUserId = userDetails.getUsername();
        UserResponseDto myInfo = userService.findUserByLoginId(loginUserId);
        return ResponseEntity.ok(myInfo);
    }

    /**
     * [PUT] /api/v1/user/nickname : 닉네임을 변경합니다.
     */
    @PutMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid NicknameUpdateRequestDto request
    ) {
        String loginUserId = userDetails.getUsername();
        userService.updateNickname(loginUserId, request.getNewNickname());
        return ResponseEntity.noContent().build();
    }

    /**
     * [PUT] /api/v1/user/password : 비밀번호를 변경합니다.
     */
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

    // --- 관리자 전용 기능 ---

    /**
     * [GET] /api/v1/user/users : 관리자만 모든 사용자 정보를 조회합니다.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // ⚠️ 관리자(ROLE_ADMIN) 권한만 허용
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}