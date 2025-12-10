package com.example.webserver.search.controller;

import com.example.webserver.search.service.FestivalSearchService;
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
 * 현재 진행 중이거나 예정된 축제 정보를 검색하는 REST API Controller.
 */
@RestController
@RequestMapping("/api/v1/festival")
@RequiredArgsConstructor
@Slf4j
public class FestivalSearchController {

    private final FestivalSearchService festivalSearchService;

    /**
     * 현재 진행 중이거나 예정된 축제 목록을 키워드(선택 사항)로 검색합니다.
     * URL 예시:
     * 1. 전체 축제: /api/v1/festival/search
     * 2. 키워드 검색: /api/v1/festival/search?query=빛축제
     *
     * @param query 검색할 키워드 (선택 사항)
     * @return 검색된 TourItemDto 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<List<TourItemDto>> searchFestivalsByKeyword(
            @RequestParam(value = "query", required = false) String query) { // (null 허용)

        // 로깅을 위해 쿼리 값 확인
        String logQuery = (query == null || query.trim().isEmpty()) ? "모든 축제" : "'" + query + "'";
        log.info("Receiving request to search festivals by keyword: {}", logQuery);

        try {
            // query가 null인 상태 그대로 Service로 전달
            List<TourItemDto> results = festivalSearchService.searchFestivals(query);

            if (results.isEmpty()) {
                log.info("No festival results found for keyword: {}", logQuery);
                return ResponseEntity.noContent().build();
            }

            log.info("Successfully retrieved {} festival items for keyword: {}", results.size(), logQuery);
            return ResponseEntity.ok(results);

        } catch (RuntimeException e) {
            log.error("Error during festival search for '{}': {}", logQuery, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}