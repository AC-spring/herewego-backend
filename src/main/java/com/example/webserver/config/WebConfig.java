package com.example.webserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 엔드포인트에 CORS 설정 적용
                .allowedOrigins(
                        "http://localhost:5173", // ✅ React 개발 환경
                        "http://localhost:5176", // ✅ 필요 시 다른 로컬 포트
                        "https://your-frontend-domain.com" // ✅ 배포 환경
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization") // ✅ 토큰(JWT) 인증을 위해 필수
                .allowCredentials(true) // ✅ 인증 정보(Credentials) 허용
                .maxAge(3600);
    }
}