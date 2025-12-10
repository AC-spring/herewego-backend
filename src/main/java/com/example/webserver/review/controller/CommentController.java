package com.example.webserver.review.controller;

import com.example.webserver.review.dto.request.CommentRequestDto;
import com.example.webserver.review.dto.response.CommentResponseDto;
import com.example.webserver.review.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
// URL 경로를 /api/v1/reviews/{reviewId}/comments 형식으로 구성합니다.
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ------------------- 댓글 작성 (POST) -------------------
    // URL: POST /api/v1/reviews/{reviewId}/comments
    @PostMapping("/comments/{reviewId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long reviewId,
            @RequestBody CommentRequestDto requestDto,
            Authentication authentication) {

        // JWT 인증을 통해 현재 로그인한 사용자의 ID를 가져옵니다.
        String loginUserId = authentication.getName();

        CommentResponseDto response = commentService.createComment(reviewId, loginUserId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }

    // ------------------- 댓글 조회 (GET) -------------------
    // URL: GET /api/v1/reviews/{reviewId}/comments
    @GetMapping("/comments/{reviewId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long reviewId) {
        List<CommentResponseDto> comments = commentService.getCommentsByReviewId(reviewId);
        return ResponseEntity.ok(comments); // 200 OK
    }

    // ------------------- 댓글 수정 (PUT) -------------------
    // URL: PUT /api/v1/reviews/comments/{commentId}
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto,
            Authentication authentication) {

        String loginUserId = authentication.getName();
        // Service에서 관리자 또는 작성자 권한을 확인하고 수정합니다.
        CommentResponseDto response = commentService.updateComment(commentId, loginUserId, requestDto);
        return ResponseEntity.ok(response); // 200 OK
    }

    // ------------------- 댓글 삭제 (DELETE) -------------------
    // URL: DELETE /api/v1/reviews/comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String loginUserId = authentication.getName();
        // Service에서 관리자 또는 작성자 권한을 확인하고 삭제합니다.
        commentService.deleteComment(commentId, loginUserId);

        // 200 OK와 함께 삭제 성공 메시지를 JSON 형태로 반환합니다.
        return ResponseEntity.ok(Collections.singletonMap("message", commentId + "번 댓글이 성공적으로 삭제되었습니다."));
    }
}