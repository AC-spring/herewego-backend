package com.example.webserver.config;

import com.example.webserver.config.jwt.JwtAuthenticationFilter;
import com.example.webserver.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    // 💡 JwtAuthenticationFilter는 필터 패키지에 정의되어 있다고 가정합니다.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT: 무상태 세션
                )
                .authorizeHttpRequests(authorize -> authorize
                        // 1. 토큰이 필요 없는 경로 설정 (POST 요청)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/reissue").permitAll()

                        // 3. 관광 API (인증된 사용자만 접근 허용)
                        .requestMatchers("/api/v1/tour/**").authenticated()

                        // 4. 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 5. JWT 필터 등록: UsernamePasswordAuthenticationFilter 이전에 실행되어야 함
                .addFilterBefore(
                        // JwtAuthenticationFilter는 JwtTokenProvider를 주입받아야 하므로 생성자를 통해 전달
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}