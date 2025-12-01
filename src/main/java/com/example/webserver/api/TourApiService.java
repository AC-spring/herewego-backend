package com.example.webserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.webserver.dto.TourItemDto;
import com.example.webserver.dto.response.TourApiResponseDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Set;

@Service
@Slf4j
public class TourApiService {

    private final WebClient tourApiWebClient;
    private final ObjectMapper objectMapper;

    private static final int NUM_OF_ROWS_PER_REGION = 12;
    private static final int FINAL_TOTAL_LIMIT = 12;
    private static final int DEFAULT_PAGE_NO = 1; // ★ 추가: pageNo 고정값 정의

    private static final String API_SERVICE_PATH = "/B551011/KorService2/areaBasedList2";

    @Value("${api.tour.service-key}")
    private String serviceKey;
    @Value("${api.tour.data-type}")
    private String dataType;

    public TourApiService(WebClient tourApiWebClient, ObjectMapper objectMapper) {
        this.tourApiWebClient = tourApiWebClient;
        this.objectMapper = objectMapper;
    }

    // --------------------------------------------------------------------------------
    // 1. 단일 지역 코드 처리 메서드 (기존 유지)
    // --------------------------------------------------------------------------------
    public String getAreaBasedList(String areaCode, int pageNo) {
        // 내부 메서드가 pageNo를 요구하므로 내부에서 1을 고정하지 않고, 이 메서드는 그대로 유지
        return getAreaBasedListInternal(areaCode, pageNo);
    }

    // --------------------------------------------------------------------------------
    // 2. 다중 지역 코드 처리 메서드 (수정됨: pageNo 파라미터 제거 및 1로 고정)
    // --------------------------------------------------------------------------------

    /**
     * 다중 지역 코드 목록을 받아 각 지역별로 아이템을 조회하고,
     * 균형 있게 배분하여 최종 12개의 아이템을 반환합니다.
     * @param areaCodes 조회할 지역 코드 리스트
     * @return 균형 있게 배분된 TourItemDto 리스트 (최대 12개)
     */
    public List<TourItemDto> getTop12ItemsByRegionGroup(List<String> areaCodes) { // ★ pageNo 제거
        String encodedServiceKey = encodeServiceKey();

        final int fixedPageNo = DEFAULT_PAGE_NO; // 1로 고정

        // 순차 처리 (concatMap 사용)
        List<String> rawResponses = Flux.fromIterable(areaCodes)
                .concatMap(areaCode -> {
                    log.info("Requesting {} items for areaCode: {} on page: {}", NUM_OF_ROWS_PER_REGION, areaCode, fixedPageNo);
                    return tourApiWebClient.get()
                            .uri(API_SERVICE_PATH, uriBuilder -> buildUri(uriBuilder, encodedServiceKey, areaCode.trim(), fixedPageNo, NUM_OF_ROWS_PER_REGION)) // ★ fixedPageNo 사용
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            log.error("API Error Response Body (Status {}). Skipping request for areaCode: {}", clientResponse.statusCode(), areaCode);
                                            return Mono.empty();
                                        });
                            })
                            .bodyToMono(String.class);
                })
                .collectList()
                .block();

        log.info("Successfully retrieved {} raw responses.", rawResponses.size());

        return parseCombineAndLimit(rawResponses, FINAL_TOTAL_LIMIT);
    }

    // --------------------------------------------------------------------------------
    // 3. 내부 유틸리티 및 파싱 메서드 (나머지 유지)
    // --------------------------------------------------------------------------------

    private String getAreaBasedListInternal(String areaCode, int pageNo) {
        String encodedServiceKey = encodeServiceKey();

        return tourApiWebClient.get()
                .uri(API_SERVICE_PATH, uriBuilder -> buildUri(uriBuilder, encodedServiceKey, areaCode, pageNo, NUM_OF_ROWS_PER_REGION))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("API Error Response Body (Status {}): {}", clientResponse.statusCode(), body);
                                throw new RuntimeException("외부 API 호출 중 오류가 발생했습니다: " + clientResponse.statusCode() + " - " + body);
                            });
                })
                .bodyToMono(String.class)
                .block();
    }

    private String encodeServiceKey() {
        try {
            return URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Service Key 인코딩 중 오류 발생", e);
            throw new RuntimeException("Service Key 인코딩 오류", e);
        }
    }

    private URI buildUri(UriBuilder uriBuilder, String encodedServiceKey, String areaCode, int pageNo, int numOfRows) {
        return uriBuilder
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("_type", dataType)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WebServerApp")
                .queryParam("areaCode", areaCode)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("arrange", "R") // 조회수 순 정렬
                .queryParam("contentTypeId", 12) // 콘텐츠 타입을 12(관광지)로 제한
                .build();
    }

    /**
     * 수정됨: 지역별 균형 배분 후, 남은 슬롯을 전체 목록 상위 아이템으로 채워 총 12개를 반환합니다.
     */
    private List<TourItemDto> parseCombineAndLimit(List<String> rawResponses, int limit) {

        // 1. 모든 응답을 파싱하여 통합 리스트 (allItems) 생성
        List<TourItemDto> allItems = rawResponses.stream()
                .flatMap(raw -> {
                    try {
                        Map<String, TourApiResponseDto> responseMap =
                                objectMapper.readValue(raw, new TypeReference<Map<String, TourApiResponseDto>>() {});

                        TourApiResponseDto responseDto = responseMap.get("response");

                        String resultCode = responseDto != null && responseDto.getHeader() != null
                                ? responseDto.getHeader().getResultCode() : "N/A";
                        List<TourItemDto> items = responseDto != null && responseDto.getBody() != null && responseDto.getBody().getItems() != null
                                ? responseDto.getBody().getItems().getItem() : null;

                        log.info("Region Response -> Result Code: {}, Items Found: {}",
                                resultCode,
                                items != null ? items.size() : 0);

                        if (items != null) {
                            return items.stream();
                        }
                    } catch (Exception e) {
                        log.error("JSON 파싱 중 심각한 오류 발생. Raw Data Snippet: {}", raw.substring(0, Math.min(raw.length(), 200)), e);
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());

        // 2. 지역 코드별로 그룹화 및 초기 균등 아이템 추출 (라운드 로빈)
        Map<String, List<TourItemDto>> groupedByArea = allItems.stream()
                .collect(Collectors.groupingBy(TourItemDto::getAreaCode));

        List<TourItemDto> finalItems = new ArrayList<>();
        Set<String> selectedContentIds = new java.util.HashSet<>(); // 중복 방지를 위해 Content ID를 저장

        int numRegions = groupedByArea.size();
        int itemsToTakePerRegion = numRegions > 0 ? limit / numRegions : 0; // 지역당 2개씩 추출

        // 2-1. 균등 배분 (라운드 로빈)
        for (List<TourItemDto> regionItems : groupedByArea.values()) {
            regionItems.stream()
                    .limit(itemsToTakePerRegion)
                    .filter(item -> selectedContentIds.add(item.getContentId())) // Content ID 중복 방지
                    .forEach(finalItems::add);
        }

        int remainingSlots = limit - finalItems.size();

        log.info("지역별 균등 배분으로 {}개를 확보했습니다. 남은 슬롯: {}", finalItems.size(), remainingSlots);

        // 2-2. 잔여 슬롯 채우기 (전체 목록에서 남은 상위 아이템 추가)
        if (remainingSlots > 0) {
            allItems.stream()
                    // 이미 선택된 아이템을 제외
                    .filter(item -> !selectedContentIds.contains(item.getContentId()))
                    // 남은 슬롯만큼만 선택
                    .limit(remainingSlots)
                    .forEach(finalItems::add);
        }

        log.info("최종적으로 총 {}개의 아이템이 반환됩니다.", finalItems.size());

        return finalItems;
    }
}