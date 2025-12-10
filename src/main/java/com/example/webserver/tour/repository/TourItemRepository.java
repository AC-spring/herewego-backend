package com.example.webserver.tour.repository;

import com.example.webserver.tour.entity.TourItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourItemRepository extends JpaRepository<TourItem, String> {

    /**
     * [분류 필터링]
     */
    List<TourItem> findByContentTypeId(String contentTypeId);

    /**
     * [해시태그 다중 필터링] 정규식을 사용하여 hashtags 필드에 모든 키워드가 포함된 여행지를 조회 (AND 조건)
     */
    @Query(value = "SELECT * FROM tour_item t WHERE t.hashtags ~ :regex", nativeQuery = true) // ✨ 쿼리에서 t.hashtags 사용
    List<TourItem> findByTagsAnd(@Param("regex") String regex);
}