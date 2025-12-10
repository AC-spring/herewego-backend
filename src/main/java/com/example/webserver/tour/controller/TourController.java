package com.example.webserver.tour.controller;

import com.example.webserver.tour.dto.TourItemDto;
import com.example.webserver.tour.service.TourApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tour")
@RequiredArgsConstructor
public class TourController {

    private final TourApiService tourApiService;

    /**
     * 🗺️ 지역 코드 매핑 테이블
     * - 광역 권역 (여러 지역 묶음)
     * - 개별 지역 (단일 지역)
     * Map.ofEntries를 사용하여 10개 이상의 항목을 안전하게 등록합니다.
     */
    private static final Map<String, List<String>> REGION_CODE_MAP = Map.ofEntries(
            // ==========================================
            // 1. 광역 권역 (그룹)
            // ==========================================
            Map.entry("수도권", List.of("1", "2", "31")),           // 서울, 인천, 경기
            Map.entry("강원권", List.of("32")),                     // 강원
            Map.entry("충청권", List.of("3", "8", "33", "34")),     // 대전, 세종, 충북, 충남
            Map.entry("전라권", List.of("5", "37", "38")),          // 광주, 전북, 전남
            Map.entry("경상권", List.of("4", "6", "7", "35", "36")),// 대구, 부산, 울산, 경북, 경남
            Map.entry("제주권", List.of("39")),                     // 제주

            // ==========================================
            // 2. 개별 지역 (단일 시/도)
            // ==========================================
            Map.entry("서울", List.of("1")),
            Map.entry("인천", List.of("2")),
            Map.entry("경기", List.of("31")),
            Map.entry("강원", List.of("32")),

            Map.entry("대전", List.of("3")),
            Map.entry("세종", List.of("8")),
            Map.entry("충북", List.of("33")),
            Map.entry("충남", List.of("34")),

            Map.entry("광주", List.of("5")),
            Map.entry("전북", List.of("37")),
            Map.entry("전남", List.of("38")),

            Map.entry("부산", List.of("6")),
            Map.entry("대구", List.of("4")),
            Map.entry("울산", List.of("7")),
            Map.entry("경북", List.of("35")),
            Map.entry("경남", List.of("36")),

            Map.entry("제주", List.of("39"))
    );

    /**
     * ✅ 통합 지역 검색 API
     * 권역 이름(예: "수도권") 또는 지역 이름(예: "서울")을 입력받아
     * 해당 지역들의 관광지 데이터를 통합 조회하여 반환합니다.
     * * @param regionName 검색할 지역명 (서울, 부산, 수도권 등)
     * @param pageNo 페이지 번호 (기본값 1)
     */
    @GetMapping("/regionList")
    public ResponseEntity<?> getTourListByRegion(
            @RequestParam(name = "regionName") String regionName,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo)
    {
        // 1. 매핑 테이블에서 해당 지역의 코드 리스트 조회
        List<String> areaCodes = REGION_CODE_MAP.get(regionName);

        // 2. 유효성 검사 (없는 지역명일 경우)
        if (areaCodes == null || areaCodes.isEmpty()) {
            // 사용 가능한 지역명 리스트를 에러 메시지에 포함
            String validRegions = String.join(", ", REGION_CODE_MAP.keySet());
            return ResponseEntity.badRequest()
                    .body("유효하지 않은 지역명입니다: [" + regionName + "]. 사용 가능: " + validRegions);
        }

        try {
            log.info("🔎 지역 검색 요청: {} -> 코드목록: {}", regionName, areaCodes);

            // 3. Service 호출 (여러 지역 코드를 받아 데이터를 조회 및 병합)
            List<TourItemDto> resultItems = tourApiService.getTop12ItemsByRegionGroup(areaCodes);

            return ResponseEntity.ok(resultItems);

        } catch (Exception e) {
            log.error("지역 검색 중 에러 발생", e);
            return ResponseEntity.internalServerError()
                    .body("검색 중 서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * (기존 유지) 단일 지역 코드 기반 조회
     * Raw Data가 필요할 때 사용
     */
    @GetMapping("/areaList")
    public ResponseEntity<?> getTourList(
            @RequestParam(name = "areaCode") String areaCode,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo)
    {
        try {
            String rawData = tourApiService.getAreaBasedList(areaCode, pageNo);
            return ResponseEntity.ok(rawData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류 발생: " + e.getMessage());
        }
    }
}