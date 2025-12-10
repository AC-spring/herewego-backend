package com.example.webserver.controller;

import com.example.webserver.dto.response.UserResponseDto;
import com.example.webserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin") // ✨ 주소가 /api/v1/admin 으로 시작합니다!
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // GET /api/v1/admin/users
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')") // SecurityConfig에서 이미 막았으면 생략 가능하지만, 이중 보안으로 둬도 됨
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // DELETE /api/v1/admin/delete-user/{userId}
    @DeleteMapping("/delete-user/{userId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUserByAdmin(userId);
        return ResponseEntity.noContent().build();
    }
}