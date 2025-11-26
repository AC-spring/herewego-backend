package com.example.webserver.service;

import com.example.webserver.config.JwtTokenProvider;
import com.example.webserver.dto.LoginRequestDto;
import com.example.webserver.dto.TokenDto; // 💡 TokenDto 임포트 추가
import com.example.webserver.dto.UserRequestDto;
import com.example.webserver.dto.UserResponseDto;
import com.example.webserver.entity.User;

import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // ----------------------------------------------------
    // 1. 회원가입 메서드 (Signup)
    // ----------------------------------------------------
    @Transactional
    public UserResponseDto signup(UserRequestDto userRequestDto) {
        if (userRepository.existsByLoginUserId(userRequestDto.getLoginUserId())) {
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }

        try {
            User user = userRequestDto.toUser(passwordEncoder);
            User savedUser = userRepository.save(user);
            return UserResponseDto.of(savedUser);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }
    }

    // ----------------------------------------------------
    // 2. 로그인 메서드 (Login)
    // ----------------------------------------------------
    /**
     * 사용자 인증을 수행하고, 성공하면 Access/Refresh 토큰을 발급합니다.
     * @param loginRequest 로그인 요청 DTO (ID, Password)
     * @return 발급된 TokenDto
     */
    // 🚨 반환 타입을 String에서 TokenDto로 변경
    public TokenDto login(LoginRequestDto loginRequest) {

        // 1. ID/Password 기반으로 인증 토큰 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getLoginUserId(),
                loginRequest.getPassword()
        );

        // 2. 실제 인증 시도 및 비밀번호 검증
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 💡 액세스/리프레시 토큰 2종 생성 및 반환
        return jwtTokenProvider.generateTokenDto(authentication);
    }
}