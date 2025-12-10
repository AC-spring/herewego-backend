package com.example.webserver.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewBoardRequestDto {

    @NotBlank(message = "제목은 필수 입력 사항입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 사항입니다.")
    private String content;

    @NotBlank(message = "지역 정보는 필수 입력 사항입니다.")
    private String region;

    @NotBlank(message = "여행지 ID는 필수 입력 사항입니다.")
    private String spotContentId;
}