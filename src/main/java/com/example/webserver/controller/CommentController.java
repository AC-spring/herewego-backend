package com.example.webserver.controller;

import com.example.webserver.dto.request.CommentRequestDto;
import com.example.webserver.dto.response.CommentResponseDto;
import com.example.webserver.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map; // ⬅️ Map을 import합니다.

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ------------------- 댓글 작성 (POST) -------------------
    @PostMapping("/comments/{reviewId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long reviewId,
            @RequestBody CommentRequestDto requestDto,
            Authentication authentication) {

        String loginUserId = authentication.getName();
        CommentResponseDto response = commentService.createComment(reviewId, loginUserId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ------------------- 댓글 조회 (GET) -------------------
    @GetMapping("/comments/{reviewId}")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long reviewId) {
        List<CommentResponseDto> comments = commentService.getCommentsByReviewId(reviewId);
        return ResponseEntity.ok(comments);
    }

    // ------------------- 댓글 수정 (PUT) -------------------
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto,
            Authentication authentication) {

        String loginUserId = authentication.getName();
        CommentResponseDto response = commentService.updateComment(commentId, loginUserId, requestDto);
        return ResponseEntity.ok(response);
    }

    // ------------------- 댓글 삭제 (DELETE) -------------------
    // ⬇️ 반환 타입을 Map으로 변경하고 200 OK 응답을 반환합니다.
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String loginUserId = authentication.getName();
        commentService.deleteComment(commentId, loginUserId);

        // ⬅️ 200 OK와 함께 성공 메시지를 JSON 형태로 반환
        return ResponseEntity.ok(Collections.singletonMap("message", commentId + "번 댓글이 성공적으로 삭제되었습니다."));
    }
}