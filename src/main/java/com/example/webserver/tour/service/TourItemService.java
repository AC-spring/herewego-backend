package com.example.webserver.tour.service;

import com.example.webserver.tour.entity.TourItem;
import com.example.webserver.tour.repository.TourItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourItemService {

    private final TourItemRepository tourItemRepository;

    /**
     * API 1: 분류 기반 필터링
     */
    public List<TourItem> getFilteredToursByClassification(String contentTypeId) {
        if (contentTypeId == null || contentTypeId.isEmpty()) {
            return tourItemRepository.findAll();
        }
        return tourItemRepository.findByContentTypeId(contentTypeId);
    }

    /**
     * API 2: 계층적 다중 해시태그 필터링 (OR 조건으로 변경됨) ✨
     * 입력된 태그 중 "하나라도" 포함하는 여행지를 조회합니다.
     * @param tags 콤마로 구분된 해시태그 리스트
     * @return 필터링된 TourItem 목록
     */
    public List<TourItem> getFilteredToursByMultipleHashtags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return tourItemRepository.findAll();
        }

        // [변경 포인트] OR 조건 정규식 생성
        // 1. 각 태그에 #을 붙임 (DB에 #태그 형태로 저장되어 있다고 가정 시, 부분 일치 방지)
        // 2. 파이프(|)로 연결하여 하나라도 일치하면 검색되도록 함
        // 예: [힐링, 바다] -> "#힐링|#바다"
        String regex = tags.stream()
                .map(tag -> "#" + tag.trim())
                .collect(Collectors.joining("|"));

        // 3. Repository 호출
        // (Repository 메서드 이름이 findByTagsAnd 여도, 넘어가는 regex가 OR 연산이므로 결과는 OR로 나옵니다)
        return tourItemRepository.findByTagsAnd(regex);
    }
}