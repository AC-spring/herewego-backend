package com.example.webserver.config;

import com.example.webserver.config.jwt.JwtAuthenticationFilter;
import com.example.webserver.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer; // Customizer 임포트 추가
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 메서드 보안 임포트 추가
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 1. ✅ 메서드 보안 활성화 (PreAuthorize, PostAuthorize 등을 사용하기 위해 필수)
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 2. ✅ CORS 설정 추가: WebMvcConfigurer에서 설정한 정책을 Spring Security에 적용
                .cors(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // 1. 토큰이 필요 없는 경로 설정 (회원가입, 로그인, 재발급)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/reissue").permitAll()

                        // 2. ✅ 관리자 전용 경로 설정: WebMvcConfigurer 설정이 아닌, URL 기반 권한 설정이 필요할 때 사용합니다.
                        //    (현재는 @PreAuthorize("hasRole('ADMIN')")를 사용하므로 이 부분은 생략 가능하나, 명확히 지정할 수도 있습니다.)
                        // .requestMatchers("/api/v1/user/users").hasRole("ADMIN")

                        // 3. 관광 API 및 마이페이지 등 인증된 사용자만 접근 허용
                        .requestMatchers("/api/v1/tour/**", "/api/v1/user/**").authenticated()

                        // 4. 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 5. JWT 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}