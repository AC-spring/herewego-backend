package com.example.webserver.controller;

import com.example.webserver.dto.request.ReviewBoardRequestDto;
import com.example.webserver.dto.response.ReviewBoardResponseDto;
import com.example.webserver.service.ReviewBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewBoardController {

    private final ReviewBoardService reviewBoardService;

    // -----------------------------------------------------------------
    // ✨ POST /api/v1/reviews 요청 처리 (게시글 생성)
    // -----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ReviewBoardResponseDto> createReview(@RequestBody @Valid ReviewBoardRequestDto requestDto) {

        // 1. 현재 로그인 사용자(작성자)의 ID를 Security Context에서 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginUserId = authentication.getName();

        // 2. 서비스 계층으로 요청 DTO와 사용자 ID를 전달
        ReviewBoardResponseDto response = reviewBoardService.createPost(loginUserId, requestDto);

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------
    // ✨ GET /api/v1/reviews/{id} 요청 처리 (게시글 단일 조회)
    // -----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ReviewBoardResponseDto> getReviewById(@PathVariable Long id) {

        // 서비스 계층에서 조회수 증가 로직을 포함하여 게시글 조회
        ReviewBoardResponseDto response = reviewBoardService.getPostById(id);

        return ResponseEntity.ok(response);
    }
}