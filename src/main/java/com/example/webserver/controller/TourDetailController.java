package com.example.webserver.controller;

import com.example.webserver.api.TourDetailCommon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 콘텐츠 ID 기반 관광지 상세 정보 조회를 위한 REST API Controller.
 * TourDetailCommon 서비스를 사용하여 단일 콘텐츠에 대한 상세 정보를 조회합니다.
 */
@RestController
@RequestMapping("/api/v1/tour/detail")
@RequiredArgsConstructor
@Slf4j
public class TourDetailController {

    private final TourDetailCommon tourDetailCommon;

    /**
     * 콘텐츠 ID를 이용해 특정 관광지의 상세 정보를 조회합니다.
     * URL 예시: /api/v1/tour/detail/common?contentId=126508
     *
     * @param contentId 상세 정보를 조회할 콘텐츠 ID (필수)
     * @return 상세 정보가 담긴 API 원시 JSON 응답 (String)
     */
    @GetMapping("/common")
    public Mono<ResponseEntity<String>> getCommonDetailByContentId(
            @RequestParam("contentId") String contentId) {

        if (contentId == null || contentId.trim().isEmpty()) {
            log.warn("[DetailSearch] Search contentId is empty."); // 로그 추가: contentId 누락 경고
            return Mono.just(ResponseEntity.badRequest().body("contentId는 필수입니다."));
        }

        log.info("[DetailSearch] Receiving request for detailcommon with contentId: {}", contentId); // 로그 추가: 요청 시작

        // Service Layer를 호출하여 API 상세 조회 로직 실행 (논블로킹)
        // pageNo는 1로 고정
        // [수정]: tourDetailCommon.getCommonDetail(String, int) 호출은 올바르나,
        // IDE/빌드가 최신 Service 클래스를 못 찾는 문제로 인해 그대로 유지합니다.
        return tourDetailCommon.getCommonDetail(contentId, 1)
                .map(rawResponse -> {
                    if (rawResponse == null || rawResponse.trim().isEmpty()) {
                        log.info("[DetailSearch] No detail response found for contentId: {}", contentId); // 로그 추가: 결과 없음
                        // 타입 불일치 오류를 해결하기 위해 <String> 제네릭을 명시
                        return ResponseEntity.noContent().<String>build();
                    }

                    // 💡 로그 추가: 응답 본문의 일부를 기록 (너무 길지 않게 200자 제한)
                    String responseSnippet = rawResponse.length() > 200 ? rawResponse.substring(0, 200) + "..." : rawResponse;
                    log.debug("[DetailSearch] Raw API Response Snippet for {}: {}", contentId, responseSnippet);

                    log.info("[DetailSearch] Successfully retrieved detail response for contentId: {}", contentId); // 로그 추가: 성공
                    return ResponseEntity.ok(rawResponse);
                })
                .onErrorResume(e -> {
                    // API 호출 또는 파싱 중 발생한 오류 처리
                    log.error("[DetailSearch] Error during detail search for '{}': {}", contentId, e.getMessage()); // 로그 추가: 에러 상세
                    return Mono.just(ResponseEntity.internalServerError().body("상세 정보 조회 중 오류가 발생했습니다."));
                });
    }
}