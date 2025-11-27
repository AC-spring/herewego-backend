// WebClientConfig.java

package com.example.webserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // application.yml에서 base-url 값을 주입
    @Value("${api.tour.base-url}")
    private String baseUrl;

    @Bean
    public WebClient tourApiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}