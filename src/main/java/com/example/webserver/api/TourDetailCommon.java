package com.example.webserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 콘텐츠 ID 기반 관광지 상세 정보를 조회하는 서비스입니다.
 * Controller의 Spring MVC 전환에 따라, 이 서비스는 내부적으로 WebClient(Reactive)를 사용하되,
 * 외부로는 동기적(Blocking) 방식인 String을 반환하도록 .block()을 적용했습니다.
 */
@Service
@Slf4j
public class TourDetailCommon {

    private final WebClient tourApiWebClient;
    private final ObjectMapper objectMapper;

    // detailCommon2 엔드포인트 (상세 정보 조회)
    private static final String API_SERVICE_PATH = "/B551011/KorService2/detailCommon2";

    @Value("${api.tour.service-key}")
    private String serviceKey;
    @Value("${api.tour.data-type}")
    private String dataType;

    public TourDetailCommon(WebClient tourApiWebClient, ObjectMapper objectMapper) {
        this.tourApiWebClient = tourApiWebClient;
        this.objectMapper = objectMapper;
    }

    // --------------------------------------------------------------------------------
    // 1. 단일 콘텐츠 상세 조회 메서드 (Spring MVC Controller용 공개 메서드)
    // --------------------------------------------------------------------------------
    /**
     * 콘텐츠 ID를 이용해 API를 동기적으로 호출하고 JSON 응답 문자열(String)을 반환합니다.
     * Service 내부의 Mono를 .block() 처리하여 Controller가 동기식으로 사용할 수 있도록 합니다.
     * * @param contentId 상세 정보를 조회할 콘텐츠 ID (필수)
     * @return API 응답 JSON 문자열 (String)
     */
    public String detailSearch(String contentId) {
        // detailCommon2에 필요한 파라미터 맵을 생성합니다.
        Map<String, String> params = new HashMap<>();
        params.put("contentId", contentId); // 입력받은 contentId 사용 (필수)
        params.put("pageNo", "1"); // 페이지 번호 1로 고정
        params.put("numOfRows", "1"); // 한 개의 데이터만 요청

        // ⬅️ callApiInternal이 반환하는 Mono<String>에 .block()을 적용하여 String을 동기적으로 추출합니다.
        // 이는 Servlet 환경에서 WebClient를 사용할 때의 표준 패턴입니다.
        return callApiInternal(API_SERVICE_PATH, params).block();
    }

    // --------------------------------------------------------------------------------
    // 2. 범용 API 호출 및 내부 유틸리티 메서드 (Mono<String> 반환 유지)
    // --------------------------------------------------------------------------------

    /**
     * 내부적으로 API를 호출하여 결과를 Mono<String>으로 반환하는 범용 메서드입니다.
     * WebClient의 논블로킹 특성을 유지합니다.
     * @param apiPath 호출할 API 엔드포인트 경로
     * @param requiredParams API에 필요한 동적 파라미터 맵
     * @return API 응답 JSON 문자열을 담은 Mono
     */
    private Mono<String> callApiInternal(String apiPath, Map<String, String> requiredParams) {
        String encodedServiceKey = encodeServiceKey();

        // 1. API 요청 시작 전, 핵심 파라미터 정보 로그 출력
        log.info("Starting Tour API Request to {}. Params: {}", apiPath, requiredParams);

        // Accept 헤더를 명시적으로 추가하여 API 서버가 응답 포맷을 명확히 인지하도록 합니다.
        MediaType mediaType = "json".equalsIgnoreCase(dataType) ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;

        return tourApiWebClient.get()
                .uri(apiPath, uriBuilder -> {
                    URI uri = buildUri(uriBuilder, encodedServiceKey, requiredParams);
                    // 요청 URI에 serviceKey가 포함되어 있으므로 디버깅을 위해 전체 URI를 출력할 수 있습니다.
                    log.debug("Final API Request URI: {}", uri);
                    return uri;
                })
                .header("Accept", mediaType.toString()) // Accept 헤더 추가
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("API Error Response Body (Status {} from {}): {}", clientResponse.statusCode(), apiPath, body);
                                // 에러를 반응형 스트림으로 전달
                                return Mono.error(new RuntimeException("외부 API 호출 중 오류가 발생했습니다: " + clientResponse.statusCode() + " - " + body));
                            });
                })
                .bodyToMono(String.class)
                // 응답이 성공적으로 왔을 때 로그 출력
                .doOnSuccess(responseBody -> {
                    log.debug("Tour API Response Success ({}). Snippet: {}", apiPath, responseBody.substring(0, Math.min(responseBody.length(), 200)) + "...");
                });
    }

    private String encodeServiceKey() {
        try {
            // **[변경 사항]** serviceKey의 값을 디버그 레벨로 로그 출력
            log.debug("Using Tour API Service Key: {}", serviceKey);

            return URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Service Key 인코딩 중 오류 발생", e);
            throw new RuntimeException("Service Key 인코딩 오류", e);
        }
    }

    /**
     * 범용적인 URI 빌더: 공통 파라미터와 동적 파라미터 맵을 모두 처리합니다.
     */
    private URI buildUri(UriBuilder uriBuilder, String encodedServiceKey, Map<String, String> dynamicParams) {
        UriBuilder builder = uriBuilder
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("_type", dataType) // 필수 (json/xml)
                .queryParam("MobileOS", "ETC") // 필수
                .queryParam("MobileApp", "WebServerApp"); // 필수

        // 동적 파라미터 맵 추가
        dynamicParams.forEach(builder::queryParam);

        return builder.build();
    }
}

