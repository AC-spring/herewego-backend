package com.example.webserver.review.dto.response;

import com.example.webserver.review.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String nickname;
    private Long reviewId; // ⬅️ postId 대신 reviewId 사용

    public static CommentResponseDto of(Comment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .nickname(comment.getUser().getNickname())
                .reviewId(comment.getReview().getId()) // ⬅️ 수정 완료
                .build();
    }
}