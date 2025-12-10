package com.example.webserver.domain.auth.Service;

import com.example.webserver.global.jwt.JwtTokenProvider;
import com.example.webserver.domain.auth.DTO.LoginRequestDto;
import com.example.webserver.domain.auth.DTO.TokenDto;

import com.example.webserver.domain.user.DTO.UserRequestDto;
import com.example.webserver.domain.user.DTO.UserResponseDto;
import com.example.webserver.domain.user.Entity.User;
import com.example.webserver.domain.user.Repository.UserRepository;
import com.example.webserver.domain.user.Exception.DuplicateUsernameException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
    @Transactional
    public TokenDto login(LoginRequestDto loginRequest) {

        // 1. ID/Password 기반으로 인증 토큰 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getLoginUserId(),
                loginRequest.getPassword()
        );

        // 2. 실제 인증 시도 및 비밀번호 검증 (Custom UserDetailsService 호출)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 액세스/리프레시 토큰 2종 생성 (JwtTokenProvider에서 role이 DTO에 포함됨)
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 4. DB 저장: Refresh Token만 해당 사용자 엔티티에 저장
        User user = userRepository.findByLoginUserId(loginRequest.getLoginUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")); // 2단계에서 이미 찾았지만 안전을 위해 다시 조회

        user.updateRefreshToken(tokenDto.getRefreshToken());
        userRepository.save(user);

        return tokenDto; // ⬅️ role 정보가 포함된 TokenDto 반환
    }

    // ----------------------------------------------------
    // ✨ 3. 토큰 재발급 메서드 (Reissue)
    // ----------------------------------------------------
    /**
     * Refresh Token을 검증하고 새로운 Access Token을 발급합니다.
     * @param tokenRequestDto 클라이언트가 보낸 Refresh Token
     * @return 새로운 Access/Refresh Token 쌍
     */
    @Transactional
    public TokenDto reissue(TokenDto tokenRequestDto) {
        String clientRefreshToken = tokenRequestDto.getRefreshToken();

        // 1. Refresh Token 유효성 및 만료 여부 검증
        if (!jwtTokenProvider.validateToken(clientRefreshToken)) {
            // RT가 만료되었거나 서명이 유효하지 않음 (Case 1)
            throw new RuntimeException("Refresh Token이 유효하지 않거나 만료되었습니다. 재로그인이 필요합니다.");
        }

        // 2. DB 일치성 검증 (핵심 보안 단계)
        User user = userRepository.findByRefreshToken(clientRefreshToken)
                .orElseThrow(() -> new RuntimeException("DB에 저장된 Refresh Token과 일치하지 않습니다. 재로그인이 필요합니다."));

        // 3. 새 Access Token 생성
        // RT의 Claims를 파싱하여 사용자 정보(Subject)를 얻고 Authentication 객체 재생성
        Claims claims = jwtTokenProvider.getClaims(clientRefreshToken);

        // 사용자 ID와 권한 정보로 Authentication 객체를 새로 만듭니다.
        // UserDetails 구현체 User 객체를 사용하여 Authorities를 가져오는 로직 필요
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), // loginUserId
                null, // 비밀번호는 필요 없음
                user.getAuthorities() // DB에서 가져온 User 객체의 권한 사용
        );

        TokenDto newTokenDto = jwtTokenProvider.generateTokenDto(authentication);
        // 이때 newTokenDto에는 이미 role 정보가 포함되어 있습니다.

        // (선택적) Refresh Token Rotation 전략: 새로운 RT를 발급하고 DB 업데이트
        // user.updateRefreshToken(newTokenDto.getRefreshToken());
        // userRepository.save(user);

        return newTokenDto;
    }

    // ----------------------------------------------------
    // ✨ 4. 로그아웃 메서드 (Logout)
    // ----------------------------------------------------
    /**
     * 로그아웃 요청을 처리합니다. Access Token으로 사용자를 식별하고 DB의 Refresh Token을 삭제합니다.
     * @param accessToken 로그아웃 요청 시 받은 Access Token
     */
    @Transactional
    public void logout(String accessToken) {

        // 1. Access Token에서 사용자 ID (loginUserId) 추출 (AT가 만료되었어도 Claims 추출 가능)
        Claims claims = jwtTokenProvider.getClaims(accessToken);
        String loginUserId = claims.getSubject();

        // 2. DB에서 사용자 조회 및 Refresh Token 삭제 (NULL 처리)
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.deleteRefreshToken();
        userRepository.save(user);

        log.info("USER LOGOUT SUCCESS: User '{}' successfully revoked Refresh Token.", loginUserId);
        // (선택적) Access Token 블랙리스트 처리 로직 추가 (남은 AT 만료 시간 동안 해당 토큰 사용 차단)
    }

    @Transactional
    public void deleteaccount(String accessToken) { // ★ 메서드 이름 변경

        // 1. Access Token에서 사용자 ID (loginUserId) 추출
        Claims claims = jwtTokenProvider.getClaims(accessToken);
        String loginUserId = claims.getSubject();

        // 2. DB에서 사용자 조회 및 영구 삭제
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("삭제할 사용자를 찾을 수 없습니다."));

        userRepository.delete(user); // JPA Repository의 delete 메서드 사용

        // (선택적) 로그 기록
        log.warn("USER ACCOUNT DELETED: User '{}' has been permanently deleted.", loginUserId);
    }
}