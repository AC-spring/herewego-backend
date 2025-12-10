package com.example.webserver.tour.controller;

import com.example.webserver.tour.dto.TourDetailCommon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import reactor.core.publisher.Mono; // ⬅️ WebFlux(Mono) 관련 임포트 제거

/**
 * 콘텐츠 ID 기반 관광지 상세 정보 조회를 위한 REST API Controller.
 * 이 컨트롤러는 Servlet 기반(Spring MVC) 환경에서 동작합니다.
 */
@RestController
@RequestMapping("/api/v1/tour/detail")
@RequiredArgsConstructor
@Slf4j
public class TourDetailController {

    // 💡 참고: tourDetailCommon 서비스의 detailSearch() 메서드는
    // 반드시 String을 반환하거나, 내부적으로 .block()을 사용해야 합니다.
    private final TourDetailCommon tourDetailCommon;

    /**
     * 콘텐츠 ID를 이용해 특정 관광지의 상세 정보를 조회합니다.
     * URL 예시: /api/v1/tour/detail/common?contentId=126508
     *
     * @param contentId 상세 정보를 조회할 콘텐츠 ID (필수)
     * @return 상세 정보가 담긴 API 원시 JSON 응답을 포함하는 ResponseEntity
     */
    @GetMapping("/common")
    // ⬅️ Spring MVC 표준: Mono<...> 대신 ResponseEntity<?>를 반환합니다.
    public ResponseEntity<?> getCommonDetailByContentId(
            @RequestParam("contentId") String contentId) {

        if (contentId == null || contentId.trim().isEmpty()) {
            log.warn("[DetailSearch] Search contentId is empty.");
            return ResponseEntity.badRequest().body("contentId는 필수입니다.");
        }

        log.info("[DetailSearch] Receiving request for detailcommon with contentId: {}", contentId);

        try {
            // Service Layer를 동기적으로 호출하여 String 결과를 받습니다.
            // 기존의 getCommonDetail(contentId, 1) 호출을 detailSearch(contentId)로 가정하고 변경합니다.
            String rawResponse = tourDetailCommon.detailSearch(contentId);

            if (rawResponse == null || rawResponse.trim().isEmpty()) {
                log.info("[DetailSearch] No detail response found for contentId: {}", contentId);
                return ResponseEntity.noContent().build();
            }

            // 로그: 응답 본문의 일부 기록
            String responseSnippet = rawResponse.length() > 200 ? rawResponse.substring(0, 200) + "..." : rawResponse;
            log.debug("[DetailSearch] Raw API Response Snippet for {}: {}", contentId, responseSnippet);

            log.info("[DetailSearch] Successfully retrieved detail response for contentId: {}", contentId);

            // 200 OK와 함께 String 본문을 반환합니다.
            return ResponseEntity.ok(rawResponse);

        } catch (Exception e) {
            // API 호출 또는 처리 중 발생한 오류 처리
            // 이 예외 처리 블록은 JWT 필터 통과 후, 서비스 로직 내에서 발생하는 예외를 처리합니다.
            log.error("[DetailSearch] Error during detail search for '{}': {}", contentId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("상세 정보 조회 중 오류가 발생했습니다.");
        }
    }
}