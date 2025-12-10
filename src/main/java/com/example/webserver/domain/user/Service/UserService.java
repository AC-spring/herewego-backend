package com.example.webserver.domain.user.Service;

import com.example.webserver.domain.user.Repository.UserRepository;
import com.example.webserver.domain.user.DTO.UserResponseDto;
import com.example.webserver.domain.user.Entity.User;
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
    private final PasswordEncoder passwordEncoder;

    // --- 마이페이지 기능 ---

    /** 현재 로그인된 사용자의 정보를 조회합니다. */
    @Transactional(readOnly = true)
    public UserResponseDto findUserByLoginId(String loginUserId) {
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserResponseDto.of(user);
    }

    /** 사용자 본인의 닉네임을 변경합니다. */
    @Transactional
    public void updateNickname(String loginUserId, String newNickname) {
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (userRepository.existsByNickname(newNickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
        user.updateNickname(newNickname);
    }

    /** 사용자 본인의 비밀번호를 변경합니다. */
    @Transactional
    public void updatePassword(String loginUserId, String currentPassword, String newPassword) {
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 업데이트
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.updatePassword(newPasswordHash);
    }

    // --- 관리자 기능 ---

    /** [관리자 전용] 모든 사용자 목록을 조회합니다. */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDto::of)
                .collect(Collectors.toList());
    }

    /** [관리자 전용] 특정 사용자를 강제 탈퇴 시킵니다. */
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("삭제할 사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId); // DB에서 사용자 레코드 삭제
    }
}