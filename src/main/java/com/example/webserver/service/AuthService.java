package com.example.webserver.service; // AuthService와 Exception이 동일 패키지

import com.example.webserver.dto.UserRequestDto;
import com.example.webserver.dto.UserResponseDto;
import com.example.webserver.entity.User;
import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 메서드
    @Transactional
    public UserResponseDto signup(UserRequestDto userRequestDto) {
        // 1. 1차 중복 검사 (속도 및 서비스 메시지 제공 목적)
        if (userRepository.existsByLoginUserId(userRequestDto.getLoginUserId())) {
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }

        try {
            // 2. DTO를 Entity로 변환 및 암호화
            User user = userRequestDto.toUser(passwordEncoder);
            userRepository.save(user);

            // 3. 응답 DTO 반환 (이제 비밀번호를 포함하지 않음)
            return UserResponseDto.of(user);

        } catch (DataIntegrityViolationException e) {
            // 4. 경합 조건(Race Condition)으로 인한 DB 롤백 예외 처리
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }
    }
}