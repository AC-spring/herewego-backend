package com.example.webserver.tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tour_item") // 실제 DB 테이블 이름
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TourItem {

    // --- 1. 식별자 및 필수 정보 ---
    @Id
    @Column(name = "content_id")
    private String contentId;

    @Column(name = "content_type_id")
    private String contentTypeId;

    @Column(name = "title")
    private String title;

    @Column(name = "tel")
    private String tel; // 전화번호

    @Column(name = "homepage")
    private String homepage; // 홈페이지 URL

    // --- 2. 이미지 정보 ---
    @Column(name = "first_image")
    private String firstImage; // 대표 이미지 URL 1

    @Column(name = "firstimage2")
    private String firstImage2; // 대표 이미지 URL 2

    // 🚨 오류 해결을 위해 'readcount'와 'overview' 필드 제거 (DB에 컬럼이 없다고 가정)
    // 데이터의 빈 칸 매핑 및 누락 컬럼으로 인한 오류 방지.

    // --- 3. 지역 코드 및 분류 ---
    @Column(name = "area_code")
    private String areaCode; // 지역 코드

    @Column(name = "sigungu_code")
    private String sigunguCode; // 시군구 코드

    @Column(name = "cat1")
    private String cat1; // 대분류

    @Column(name = "cat2")
    private String cat2; // 중분류

    @Column(name = "cat3")
    private String cat3; // 소분류

    // --- 4. 주소 및 좌표 정보 ---
    @Column(name = "addr1")
    private String address; // 대표 주소

    @Column(name = "addr2")
    private String detailAddress; // 상세 주소

    @Column(name = "mapx")
    private Double mapx; // X좌표 (경도)

    @Column(name = "mapy")
    private Double mapy; // Y좌표 (위도)

    @Column(name = "mlevel")
    private Integer mlevel; // 지도 레벨

    // --- 5. 해시태그 필드 (핵심 필드) ---
    /**
     * DB 컬럼 이름 'hashtags'에 매핑됩니다.
     */
    @Column(name = "hashtags")
    private String tag;
}