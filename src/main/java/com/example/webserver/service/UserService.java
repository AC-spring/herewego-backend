package com.example.webserver.service;

import com.example.webserver.dto.response.UserResponseDto;
import com.example.webserver.entity.User;
import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화/비교를 위해 필요

    // 1. 사용자 본인 정보 조회 (마이페이지)
    @Transactional(readOnly = true)
    public UserResponseDto findUserByLoginId(String loginUserId) {
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UserResponseDto.of(user);
    }

    // 2. 닉네임 변경 (마이페이지)
    @Transactional
    public void updateNickname(String loginUserId, String newNickname) {
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (userRepository.existsByNickname(newNickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(newNickname);
    }

    // 3. 비밀번호 변경 (마이페이지)
    @Transactional
    public void updatePassword(String loginUserId, String currentPassword, String newPassword) {
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.updatePassword(newPasswordHash);
    }

    // 4. 관리자 전용: 모든 사용자 정보 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponseDto::of)
                .collect(Collectors.toList());
    }
}