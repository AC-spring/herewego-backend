package com.example.webserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data // Lombok을 사용하여 Getter, Setter, toString 등을 자동 생성
public class TourItemDto {

    // --- 1. 핵심 식별자 및 필수 정보 ---
    @JsonProperty("contentid")
    private String contentId; // 콘텐츠 ID (고유 식별자)

    @JsonProperty("contenttypeid")
    private String contentTypeId; // 콘텐츠 타입 ID (12: 관광지, 14: 문화시설 등)

    @JsonProperty("title")
    private String title; // 제목

    @JsonProperty("addr1")
    private String address; // 대표 주소 (addr1)


    // --- 2. 지역 코드 정보 (모든 검색 API에서 공통) ---
    @JsonProperty("areacode")
    private String areaCode; // 지역 코드 (1=서울, 31=경기 등)


    // --- 3. 상세 정보 (검색 시 유용하며, 없으면 null 처리됨) ---

    @JsonProperty("addr2")
    private String detailAddress; // 상세 주소 (addr2)

    @JsonProperty("tel")
    private String tel; // 전화번호

    @JsonProperty("firstimage")
    private String firstImage; // 대표 이미지 URL (소형)

    @JsonProperty("firstimage2")
    private String firstImage2; // 대표 이미지 URL (대형)

    // --- 4. 기타 정보 ---
    @JsonProperty("readcount")
    private Integer readCount; // 조회수 (정렬 기준 'R'에서 사용)

    @JsonProperty("overview")
    private Integer overview; // 관광지 정보 (정렬 기준 'R'에서 사용)


}