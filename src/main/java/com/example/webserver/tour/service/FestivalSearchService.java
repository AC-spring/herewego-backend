package com.example.webserver.tour.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.example.webserver.tour.dto.TourItemDto;
import com.example.webserver.tour.dto.response.TourApiResponseDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

/**
 * 축제/행사 전용 검색 서비스.
 * searchFestival2 API를 사용하며, 오늘 날짜를 기준으로 기간이 지난 축제는 제외하고,
 * API 오류 응답을 안전하게 처리하며, 키워드 없이 전체 조회를 지원합니다.
 */
@Service
@Slf4j
public class FestivalSearchService {

    private final WebClient tourApiWebClient;
    private final ObjectMapper objectMapper;

    private static final String FESTIVAL_SERVICE_PATH = "/B551011/KorService2/searchFestival2";
    private static final int FESTIVAL_SEARCH_LIMIT = 30;
    private static final int FESTIVAL_TOTAL_LIMIT = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${api.tour.service-key}")
    private String serviceKey;
    @Value("${api.tour.data-type}")
    private String dataType;

    public FestivalSearchService(WebClient tourApiWebClient, ObjectMapper objectMapper,
                                 @Value("${api.tour.service-key}") String serviceKey,
                                 @Value("${api.tour.data-type}") String dataType) {
        this.tourApiWebClient = tourApiWebClient;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.serviceKey = serviceKey;
        this.dataType = dataType;
    }

    // --------------------------------------------------------------------------------
    // 1. 축제 검색 처리 메서드 (오류 방어 로직 포함)
    // --------------------------------------------------------------------------------
    public List<TourItemDto> searchFestivals(String keyword) {
        String encodedServiceKey = encodeServiceKey();

        String logQuery = (keyword == null || keyword.trim().isEmpty()) ? "모든 축제" : "'" + keyword + "'";
        log.info("Requesting searchFestival2 for keyword: {}", logQuery);

        String rawResponse = tourApiWebClient.get()
                .uri(FESTIVAL_SERVICE_PATH, uriBuilder -> buildFestivalUri(uriBuilder, encodedServiceKey, keyword, FESTIVAL_SEARCH_LIMIT))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("API Error Response Body (Status {}): {}", clientResponse.statusCode(), body);
                                throw new RuntimeException("축제 검색 API 호출 중 오류가 발생했습니다: " + clientResponse.statusCode() + " - " + body);
                            });
                })
                .bodyToMono(String.class)
                .block();

        if (rawResponse != null) {
            // ✨ API 오류 응답 방어 로직: 오류 코드 10이 포함된 경우 파싱 시도 없이 빈 리스트 반환
            if (rawResponse.contains("\"resultCode\":\"10\"")) {
                log.error("API Request Failed with Error Code 10. Returning empty list: {}", rawResponse);
                return new ArrayList<>();
            }

            return parseAndLimitFestivalResults(rawResponse, FESTIVAL_TOTAL_LIMIT);
        }

        return new ArrayList<>();
    }

    // --------------------------------------------------------------------------------
    // 2. 내부 유틸리티 및 파싱 메서드
    // --------------------------------------------------------------------------------

    private String encodeServiceKey() {
        try {
            return URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Service Key 인코딩 중 오류 발생", e);
            throw new RuntimeException("Service Key 인코딩 오류", e);
        }
    }

    /**
     * searchFestival2 API 호출을 위한 URI 빌더 (오늘 날짜 포함, keyword는 선택적으로 포함)
     */
    private URI buildFestivalUri(UriBuilder uriBuilder, String encodedServiceKey, String keyword, int numOfRows) {
        String today = LocalDate.now().format(DATE_FORMATTER);

        UriBuilder finalUriBuilder = uriBuilder
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("_type", dataType)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WebServerApp")
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", 1)
                .queryParam("arrange", "R")
                .queryParam("eventStartDate", today); // 오늘 날짜로 필터링

        // ✨ 키워드가 null이거나 비어있지 않을 때만 파라미터에 추가 (전체 조회 허용)
        if (keyword != null && !keyword.trim().isEmpty()) {
            finalUriBuilder.queryParam("keyword", keyword);
        }

        return finalUriBuilder.build();
    }

    /**
     * 축제 검색 응답을 파싱, 이미지 필터링 후 반환합니다.
     */
    private List<TourItemDto> parseAndLimitFestivalResults(String rawResponse, int limit) {
        List<TourItemDto> allItems = new ArrayList<>();
        Set<String> selectedContentIds = new java.util.HashSet<>();

        try {
            Map<String, TourApiResponseDto> responseMap =
                    objectMapper.readValue(rawResponse, new TypeReference<Map<String, TourApiResponseDto>>() {});

            TourApiResponseDto responseDto = responseMap.get("response");

            String resultCode = responseDto != null && responseDto.getHeader() != null
                    ? responseDto.getHeader().getResultCode() : "N/A";

            List<TourItemDto> items = responseDto != null && responseDto.getBody() != null && responseDto.getBody().getItems() != null
                    ? responseDto.getBody().getItems().getItem() : null;

            log.info("Festival Search Response -> Result Code: {}, Items Found: {}",
                    resultCode,
                    items != null ? items.size() : 0);

            if (items != null) {
                items.stream()
                        .filter(item -> item.getFirstImage() != null && !item.getFirstImage().trim().isEmpty())
                        .filter(item -> selectedContentIds.add(item.getContentId()))
                        .limit(limit)
                        .forEach(allItems::add);
            }
        } catch (Exception e) {
            log.error("JSON 파싱 중 심각한 오류 발생 (정상 응답 파싱 실패 가능성): {}", rawResponse.substring(0, Math.min(rawResponse.length(), 200)), e);
        }

        log.info("축제 검색 최종적으로 총 {}개의 아이템이 반환됩니다. (필터링 완료, 목표: {})", allItems.size(), limit);

        return allItems;
    }
}