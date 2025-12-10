package com.example.webserver.tour.controller;

import com.example.webserver.tour.entity.TourItem;
import com.example.webserver.tour.service.TourItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tour") // SecurityConfig 경로와 일치
@RequiredArgsConstructor
public class TourItemController {

    private final TourItemService tourItemService;

    // API 1: /filter 유지
    @GetMapping("/filter")
    public ResponseEntity<List<TourItem>> getFilteredTours(
            @RequestParam(required = false) String contentTypeId) {

        List<TourItem> filteredList = tourItemService.getFilteredToursByClassification(contentTypeId);
        return ResponseEntity.ok(filteredList);
    }

    /**
     * API 2: 계층적 다중 해시태그 필터링 (AND 조건)
     * GET /api/v1/tour/search-tags?hashtags=tag1,tag2,...
     */
    @GetMapping("/search-tags")
    public ResponseEntity<List<TourItem>> searchToursByMultipleHashtags(
            // ✨ 파라미터 이름을 'hashtags'로 받음
            @RequestParam(required = true) List<String> hashtags) {

        List<TourItem> filteredList = tourItemService.getFilteredToursByMultipleHashtags(hashtags);

        return ResponseEntity.ok(filteredList);
    }
}