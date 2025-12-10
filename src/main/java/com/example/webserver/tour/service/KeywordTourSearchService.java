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
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

/**
 * 일반 관광지 키워드 기반 검색 서비스 (기존 searchKeyword2 API 사용)
 * 축제 기간 필터링 로직은 FestivalSearchService로 분리됨.
 */
@Service
@Slf4j
public class KeywordTourSearchService {

    private final WebClient tourApiWebClient;
    private final ObjectMapper objectMapper;

    // --------------------------------------------------------------------------------
    // 상 수 (기존 searchKeyword2 설정 유지)
    // --------------------------------------------------------------------------------
    private static final int KEYWORD_SEARCH_LIMIT = 30;
    private static final int KEYWORD_TOTAL_LIMIT = 30;
    private static final String KEYWORD_SERVICE_PATH = "/B551011/KorService2/searchKeyword2";

    @Value("${api.tour.service-key}")
    private String serviceKey;
    @Value("${api.tour.data-type}")
    private String dataType;

    public KeywordTourSearchService(WebClient tourApiWebClient, ObjectMapper objectMapper) {
        this.tourApiWebClient = tourApiWebClient;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    // --------------------------------------------------------------------------------
    // 1. 키워드 검색 처리 메서드
    // --------------------------------------------------------------------------------
    public List<TourItemDto> searchDataByKeyword(String keyword) {
        String encodedServiceKey = encodeServiceKey();

        log.info("Requesting searchKeyword2 for keyword: {}", keyword);

        String rawResponse = tourApiWebClient.get()
                .uri(KEYWORD_SERVICE_PATH, uriBuilder -> buildKeywordUri(uriBuilder, encodedServiceKey, keyword, KEYWORD_SEARCH_LIMIT))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("API Error Response Body (Status {}): {}", clientResponse.statusCode(), body);
                                throw new RuntimeException("키워드 검색 API 호출 중 오류가 발생했습니다: " + clientResponse.statusCode() + " - " + body);
                            });
                })
                .bodyToMono(String.class)
                .block();

        if (rawResponse != null) {
            // 이 서비스는 축제 기간 필터링을 하지 않습니다.
            return parseAndLimitKeywordResults(rawResponse, KEYWORD_TOTAL_LIMIT);
        }

        return new ArrayList<>();
    }

    // --------------------------------------------------------------------------------
    // 2. 내부 유틸리티 및 파싱 메서드 (기존 로직 유지)
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
     * searchKeyword2 API 호출을 위한 URI 빌더
     */
    private URI buildKeywordUri(UriBuilder uriBuilder, String encodedServiceKey, String keyword, int numOfRows) {
        return uriBuilder
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("_type", dataType)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WebServerApp")
                .queryParam("keyword", keyword)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", 1)
                .queryParam("arrange", "R")
                .build();
    }

    /**
     * 키워드 검색 응답을 파싱, 이미지 필터링 후 반환합니다.
     */
    private List<TourItemDto> parseAndLimitKeywordResults(String rawResponse, int limit) {
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

            log.info("Keyword Search Response -> Result Code: {}, Items Found: {}",
                    resultCode,
                    items != null ? items.size() : 0);

            if (items != null) {
                items.stream()
                        .filter(item -> item.getFirstImage() != null && !item.getFirstImage().trim().isEmpty())
                        .limit(limit)
                        .filter(item -> selectedContentIds.add(item.getContentId()))
                        .forEach(allItems::add);
            }
        } catch (Exception e) {
            log.error("JSON 파싱 중 심각한 오류 발생. Raw Data Snippet: {}", rawResponse.substring(0, Math.min(rawResponse.length(), 200)), e);
        }

        log.info("키워드 검색 최종적으로 총 {}개의 아이템이 반환됩니다. (필터링 완료, 목표: {})", allItems.size(), limit);

        return allItems;
    }
}