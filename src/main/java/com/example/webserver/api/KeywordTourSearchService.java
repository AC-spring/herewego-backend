package com.example.webserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.example.webserver.dto.TourItemDto;
import com.example.webserver.dto.response.TourApiResponseDto;

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
import java.util.stream.Stream;

@Service
@Slf4j
public class KeywordTourSearchService {

    private final WebClient tourApiWebClient;
    private final ObjectMapper objectMapper;

    // --------------------------------------------------------------------------------
    // ★ 수정된 상수: 아이템 개수 12개 -> 30개로 변경
    // --------------------------------------------------------------------------------
    private static final int KEYWORD_SEARCH_LIMIT = 30; // 👈 API 요청 시 가져올 아이템 수 (30개로 증가)
    private static final int KEYWORD_TOTAL_LIMIT = 30; // 👈 최종 반환할 아이템 수 제한 (30개로 증가)
    private static final String KEYWORD_SERVICE_PATH = "/B551011/KorService2/searchKeyword2";


    @Value("${api.tour.service-key}")
    private String serviceKey;
    @Value("${api.tour.data-type}")
    private String dataType;

    public KeywordTourSearchService(WebClient tourApiWebClient, ObjectMapper objectMapper) {
        this.tourApiWebClient = tourApiWebClient;
        // JSON 파싱 오류 해결 설정 유지
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    // --------------------------------------------------------------------------------
    // 1. 키워드 검색 처리 메서드
    // --------------------------------------------------------------------------------
    /**
     * 키워드를 기반으로 관광지 정보를 검색하고 파싱하여 DTO 리스트로 반환합니다.
     * @param keyword 검색할 키워드 (예: "쇼핑", "문화", "음식")
     * @return 파싱되어 필터링된 TourItemDto 리스트 (최대 30개)
     */
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
            return parseAndLimitKeywordResults(rawResponse, KEYWORD_TOTAL_LIMIT);
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
                .queryParam("arrange", "R") // 💡 제목순 정렬
                .build();
    }

    /**
     * 키워드 검색 응답을 파싱하여 이미지가 있는 아이템만 필터링하고 최대 30개로 제한 후 반환합니다.
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
                // 💡 이미지 필터링 및 아이템 선택
                items.stream()
                        // 1. 이미지가 있는 항목만 필터링
                        .filter(item -> item.getFirstImage() != null && !item.getFirstImage().trim().isEmpty())
                        // 2. 최대 개수 제한
                        .limit(limit)
                        // 3. 중복 contentId 제거
                        .filter(item -> selectedContentIds.add(item.getContentId()))
                        .forEach(allItems::add);
            }
        } catch (Exception e) {
            log.error("JSON 파싱 중 심각한 오류 발생. Raw Data Snippet: {}", rawResponse.substring(0, Math.min(rawResponse.length(), 200)), e);
        }

        log.info("키워드 검색 최종적으로 총 {}개의 아이템이 반환됩니다. (이미지 필터링 완료, 목표: {})", allItems.size(), limit);

        return allItems;
    }
}