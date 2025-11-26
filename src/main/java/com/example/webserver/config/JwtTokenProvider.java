package com.example.webserver.config;

import com.example.webserver.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Authentication 객체를 받아 액세스 토큰과 리프레시 토큰을 모두 생성합니다.
     */
    public TokenDto generateTokenDto(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // 1. 액세스 토큰 생성 (만료 시간 짧게)
        Date accessTokenExpiresIn = new Date(now + accessTokenExpiration);
        String accessToken = Jwts.builder()
                .subject(authentication.getName())
                .claim("auth", authorities)
                .issuedAt(new Date(now))
                .expiration(accessTokenExpiresIn)
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        // 2. 리프레시 토큰 생성 (만료 시간 길게)
        String refreshToken = Jwts.builder()
                .expiration(new Date(now + refreshTokenExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .build();
    }

    // JWT 토큰을 복호화하여 인증 객체 반환 (유효한 토큰에만 사용)
    public Authentication getAuthentication(String token) {
        // Claims 추출 (여기서는 만료되지 않은 토큰이 들어와야 함)
        Claims claims = getClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다. (재발급 요청 가능)");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // ------------------------------------------------------------------
    // ✨ 추가된 핵심 메서드 1: Claims 추출 (만료된 토큰도 가능)
    // ------------------------------------------------------------------
    /**
     * 토큰에서 Claims를 추출합니다. 만료된 토큰의 경우에도 Subject(사용자 이름)을 얻기 위해 사용됩니다.
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료되었어도 Claims는 반환하여 사용자 식별에 사용
            return e.getClaims();
        } catch (Exception e) {
            log.error("Claims 추출 중 오류 발생", e);
            throw new RuntimeException("Invalid token during claims extraction.");
        }
    }

    // ------------------------------------------------------------------
    // ✨ 추가된 핵심 메서드 2: 만료 여부만 판단
    // ------------------------------------------------------------------
    /**
     * 토큰의 만료 여부만 판단하여 Access Token 재발급 필요성을 확인합니다.
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return false; // 만료되지 않음
        } catch (ExpiredJwtException e) {
            return true; // 만료됨
        } catch (Exception e) {
            // 서명 오류, 형식 오류 등은 만료로 간주하지 않음 (validateToken에서 처리)
            return false;
        }
    }
}