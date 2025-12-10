package com.example.webserver.tour.service;

import com.example.webserver.tour.entity.TourItem;
import com.example.webserver.tour.repository.TourItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service // ✨ 1. Spring Bean으로 등록되도록 @Service 어노테이션 추가
@RequiredArgsConstructor
public class TourItemService {

    private final TourItemRepository tourItemRepository;

    /**
     * API 1: 분류 기반 필터링
     * contentTypeId (예: 12=관광지)를 기준으로 여행지 목록을 조회합니다.
     * @param contentTypeId 조회할 분류 ID
     * @return 필터링된 TourItem 목록
     */
    public List<TourItem> getFilteredToursByClassification(String contentTypeId) {
        if (contentTypeId == null || contentTypeId.isEmpty()) {
            // contentTypeId가 없으면 전체 목록 반환
            return tourItemRepository.findAll();
        }
        // JpaRepository의 findByContentTypeId 규칙 사용
        return tourItemRepository.findByContentTypeId(contentTypeId);
    }

    /**
     * API 2: 계층적 다중 해시태그 필터링 (AND 조건)
     * 입력된 모든 태그를 포함하는 (AND 조건) 여행지를 조회합니다.
     * PostgreSQL의 정규식 검색을 활용합니다.
     * @param tags 콤마로 구분된 해시태그 리스트 (Controller에서 List<String> 형태로 넘어옴)
     * @return 필터링된 TourItem 목록
     */
    public List<TourItem> getFilteredToursByMultipleHashtags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return tourItemRepository.findAll();
        }

        // 1. 입력된 각 태그에 #을 붙여서 정규식 그룹으로 변환
        // 예: [힐링여행, 공원] -> #힐링여행, #공원
        String searchTags = tags.stream()
                .map(tag -> "#" + tag)
                .collect(Collectors.joining("|")); // 정규식에 필요한 구분자는 Service 로직에 따라 다름 (AND 조건 구현에 따라 달라짐)


        // 2. AND 조건 정규식 생성: 모든 태그가 반드시 포함되어야 함 (?=.*#tag)
        // 예: #힐링여행, #공원 -> (?=.*#힐링여행)(?=.*#공원)
        // 이 정규식은 문자열 내에 각 태그(#tag 포함)가 모두 존재하는지 확인합니다.
        String regex = tags.stream()
                .map(tag -> "(?=.*#" + tag.trim() + ")")
                .collect(Collectors.joining());


        // 3. Repository의 Native Query 호출
        return tourItemRepository.findByTagsAnd(regex);
    }
}