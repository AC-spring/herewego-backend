package com.example.webserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS 정책을 정의하는 설정 파일 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 CORS 설정 적용
                .allowedOrigins(
                        "http://localhost:5173", // React 개발 환경 1
                        "http://localhost:5176"  // React 개발 환경 2
                        // 프론트엔드 배포 시 여기에 해당 URL을 추가해야 함
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("Content-Type", "Authorization") // 허용할 요청 헤더 (JWT 필수)
                .allowCredentials(true) // 인증 정보(쿠키, Authorization 헤더) 허용
                .maxAge(3600); // Pre-flight 캐시 시간
    }
}