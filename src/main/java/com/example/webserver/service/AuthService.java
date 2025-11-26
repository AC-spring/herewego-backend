package com.example.webserver.service;

import com.example.webserver.dto.LoginRequestDto; // 💡 로그인 요청 DTO 임포트
import com.example.webserver.dto.UserRequestDto;
import com.example.webserver.dto.UserResponseDto;
import com.example.webserver.entity.User;
import com.example.webserver.config.JwtTokenProvider; // 💡 JWT Provider 임포트
import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 💡 인증 토큰 임포트
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // 💡 인증 관리자 빌더 임포트
import org.springframework.security.core.Authentication; // 💡 인증 객체 임포트
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // 💡 추가: JWT 생성 및 관리
    private final AuthenticationManagerBuilder authenticationManagerBuilder; // 💡 추가: 로그인 인증 처리

    // ----------------------------------------------------
    // 1. 회원가입 메서드 (Signup)
    // ----------------------------------------------------
    @Transactional
    public UserResponseDto signup(UserRequestDto userRequestDto) {
        // 1. 1차 중복 검사
        if (userRepository.existsByLoginUserId(userRequestDto.getLoginUserId())) {
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }

        try {
            // 2. DTO를 Entity로 변환 및 암호화
            User user = userRequestDto.toUser(passwordEncoder);
            User savedUser = userRepository.save(user);

            // 3. 응답 DTO 반환
            return UserResponseDto.of(savedUser);

        } catch (DataIntegrityViolationException e) {
            // 4. 경합 조건으로 인한 DB 롤백 예외 처리
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }
    }

    // ----------------------------------------------------
    // 2. 로그인 메서드 (Login)
    // ----------------------------------------------------
    /**
     * 사용자 인증을 수행하고, 성공하면 JWT 토큰을 발급합니다.
     * @param loginRequest 로그인 요청 DTO (ID, Password)
     * @return 발급된 JWT 토큰 문자열
     */
    public String login(LoginRequestDto loginRequest) {

        // 1. ID/Password 기반으로 인증 토큰 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getLoginUserId(),
                loginRequest.getPassword()
        );

        // 2. 실제 인증 시도 및 비밀번호 검증
        // CustomUserDetailsService의 loadUserByUsername이 호출되어 인증을 처리합니다.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String jwtToken = jwtTokenProvider.generateToken(authentication);

        // 4. 토큰 반환
        return jwtToken;
    }
}