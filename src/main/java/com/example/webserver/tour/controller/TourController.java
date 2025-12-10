package com.example.webserver.tour.controller;

import com.example.webserver.tour.service.TourApiService;
import com.example.webserver.tour.dto.TourItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tour")
@RequiredArgsConstructor
public class TourController {

    private final TourApiService tourApiService;

    // 6대 광역 권역별 지역 코드 매핑 정보 (Controller에서 관리)
    private static final Map<String, List<String>> REGION_CODE_MAP = Map.of(
            "수도권", List.of("1", "2", "31"), // 서울, 인천, 경기도
            "강원권", List.of("32"),           // 강원특별자치도
            "충청권", List.of("3", "8", "33", "34"), // 대전, 세종, 충북, 충남
            "전라권", List.of("5", "37", "38"),      // 광주, 전북, 전남
            "경상권", List.of("4", "6", "7", "35", "36"), // 대구, 부산, 울산, 경북, 경남
            "제주권", List.of("39")            // 제주특별자치도
    );

    /**
     * GET /api/v1/tour/areaList
     * 단일 지역 기반 관광 정보를 조회하는 기존 API 엔드포인트입니다.
     */
    @GetMapping("/areaList")
    public ResponseEntity<?> getTourList(
            @RequestParam(name = "areaCode") String areaCode,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo)
    {
        try {
            // 단일 지역 코드 검색 로직 (기존)
            String rawData = tourApiService.getAreaBasedList(areaCode, pageNo);
            return ResponseEntity.ok(rawData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("단일 지역 API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------------
    // ✨ 새로운 API 엔드포인트: 광역 권역별 통합 검색
    // ----------------------------------------------------------------------

    /**
     * GET /api/v1/tour/regionList
     * 6대 광역 권역별로 데이터를 통합하여 상위 12개 아이템을 조회하는 API입니다.
     * @param regionName 조회할 권역 명칭 (예: "수도권", "경상권")
     * @return 통합된 TourItemDto 리스트
     */
    @GetMapping("/regionList")
    public ResponseEntity<?> getTourListByRegion(
            @RequestParam(name = "regionName") String regionName,
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo)
    {
        // 1. 권역 이름 유효성 검사 및 코드 리스트 조회
        List<String> areaCodes = REGION_CODE_MAP.get(regionName);

        if (areaCodes == null || areaCodes.isEmpty()) {
            String validRegions = String.join(", ", REGION_CODE_MAP.keySet());
            return ResponseEntity.badRequest().body("유효하지 않은 권역 명칭입니다. 사용 가능한 권역: " + validRegions);
        }

        try {
            // 2. TourApiService의 통합 검색 메서드 호출
            // 이 메서드는 모든 지역의 데이터를 통합하여 최종 12개 아이템만 반환합니다.
            List<TourItemDto> top12Items = tourApiService.getTop12ItemsByRegionGroup(areaCodes);

            // 3. 성공 응답 (DTO 리스트를 JSON 형태로 반환)
            return ResponseEntity.ok(top12Items);

        } catch (Exception e) {
            // 4. 오류 처리
            return ResponseEntity.internalServerError().body("권역 통합 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}