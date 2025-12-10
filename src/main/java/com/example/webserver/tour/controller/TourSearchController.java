package com.example.webserver.tour.controller;

import com.example.webserver.tour.service.KeywordTourSearchService;
import com.example.webserver.tour.dto.TourItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 키워드 기반 관광 정보 검색을 위한 REST API Controller.
 */
@RestController
@RequestMapping("/api/v1/tour/search")
@RequiredArgsConstructor
@Slf4j
public class TourSearchController {

    private final KeywordTourSearchService keywordTourSearchService;

    /**
     * 특정 키워드를 이용해 관광지 목록을 검색합니다.
     * URL 예시: /api/v1/tour/search/keyword?query=쇼핑
     *
     * @param query 검색할 키워드 (필수)
     * @return 검색된 TourItemDto 리스트
     */
    @GetMapping("/keyword")
    public ResponseEntity<List<TourItemDto>> searchByKeyword(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Search keyword is empty.");
            return ResponseEntity.badRequest().build();
        }

        log.info("Receiving request to search by keyword: {}", query);

        try {
            // Service Layer를 호출하여 API 검색 및 파싱 로직 실행
            List<TourItemDto> results = keywordTourSearchService.searchDataByKeyword(query);

            if (results.isEmpty()) {
                log.info("No results found for keyword: {}", query);
                return ResponseEntity.noContent().build();
            }

            log.info("Successfully retrieved {} items for keyword: {}", results.size(), query);
            return ResponseEntity.ok(results);

        } catch (RuntimeException e) {
            // API 호출 또는 파싱 중 발생한 오류 처리
            log.error("Error during keyword search for '{}': {}", query, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}