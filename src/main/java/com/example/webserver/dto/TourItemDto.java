package com.example.webserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data // Lombok을 사용하여 Getter, Setter, toString 등을 자동 생성
public class TourItemDto {

    // 필수 정보
    @JsonProperty("contentid")
    private String contentId; // 콘텐츠 ID (고유 식별자)

    @JsonProperty("contenttypeid")
    private String contentTypeId; // 콘텐츠 타입 ID (12: 관광지, 14: 문화시설 등)

    @JsonProperty("title")
    private String title; // 제목

    @JsonProperty("addr1")
    private String address; // 대표 주소


    // 지역 코드 (어떤 지역인지 구분하기 위해 필요)
    @JsonProperty("areacode")
    private String areaCode; // 지역 코드 (1=서울, 31=경기 등)

    // 추가 정보
    @JsonProperty("firstimage")
    private String firstImage; // 대표 이미지 URL (소형)

    @JsonProperty("readcount")
    private Integer readCount; // 조회수 (정렬 기준 'R'에서 사용)
}