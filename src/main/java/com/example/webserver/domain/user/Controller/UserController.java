package com.example.webserver.domain.user.Controller;

import com.example.webserver.domain.user.DTO.NicknameUpdateRequestDto;
import com.example.webserver.domain.user.DTO.PasswordUpdateRequestDto;
import com.example.webserver.domain.user.DTO.UserResponseDto;
import com.example.webserver.domain.user.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 권한 검사 어노테이션
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

    // GET /api/v1/user/users : 모든 사용자 정보 조회
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // ROLE_ADMIN만 접근 허용
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }



    // DELETE /api/v1/user/admin/delete-user/{userId} : 강제 탈퇴
    @DeleteMapping("/admin/delete-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUserByAdmin(userId);
        return ResponseEntity.noContent().build();
    }
}