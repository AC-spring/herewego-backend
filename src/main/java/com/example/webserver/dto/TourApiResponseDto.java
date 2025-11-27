package com.example.webserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TourApiResponseDto {

    // API 응답의 Header (결과 코드, 메시지 포함)
    @JsonProperty("header")
    private Header header;

    // API 응답의 Body (데이터, 페이징 정보 포함)
    @JsonProperty("body")
    private Body body;

    /**
     * Header DTO (결과 코드 및 메시지)
     */
    @Data
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode; // 결과 코드 (예: 0000 = 성공)

        @JsonProperty("resultMsg")
        private String resultMsg; // 결과 메시지
    }

    /**
     * Body DTO (아이템 목록 및 페이징 정보)
     */
    @Data
    public static class Body {

        // 아이템 목록 (TourApiResponseItemsDto와 연결)
        @JsonProperty("items")
        private TourApiResponseItemsDto items;

        // 페이징 정보
        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }
}