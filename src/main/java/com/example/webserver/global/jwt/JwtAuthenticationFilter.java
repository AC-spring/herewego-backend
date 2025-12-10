package com.example.webserver.global.jwt;

import com.example.webserver.domain.auth.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    // 요청 헤더에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        logger.info(String.valueOf(1));
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            logger.info(String.valueOf(2));
            return bearerToken.substring(BEARER_PREFIX.length());

        }
        return null;
    }

    // 실제 필터링 로직
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 토큰 추출
        String jwt = resolveToken(request);
        logger.info("Authorization = {}", request.getHeader("Authorization"));
        logger.info("jwt = {}", jwt);
        logger.info("validate = {}", jwt != null ? jwtTokenProvider.validateToken(jwt) : null);


        // 2. validateToken으로 토큰 유효성 검사
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            // 토큰이 유효할 경우 SecurityContext에 인증 정보 저장
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
           logger.info(authentication.toString());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info(String.valueOf(3));
        }
        logger.info(String.valueOf(4));
        filterChain.doFilter(request, response);
    }
}