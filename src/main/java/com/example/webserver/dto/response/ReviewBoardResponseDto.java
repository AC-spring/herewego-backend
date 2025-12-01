package com.example.webserver.dto.response;

import com.example.webserver.entity.ReviewBoard;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewBoardResponseDto {
    private Long id;
    private String title;
    private String content;
    private String authorId;
    private int viewCount;
    private String region;
    private String spotContentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewBoardResponseDto of(ReviewBoard board) {
        return ReviewBoardResponseDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorId(board.getUser().getLoginUserId())
                .viewCount(board.getViewCount())
                .region(board.getRegion())
                .spotContentId(board.getSpotContentId())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}