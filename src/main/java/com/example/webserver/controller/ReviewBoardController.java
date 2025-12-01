package com.example.webserver.controller;

import com.example.webserver.dto.request.ReviewBoardRequestDto;
import com.example.webserver.dto.response.ReviewBoardResponseDto;
import com.example.webserver.service.ReviewBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewBoardController {

    private final ReviewBoardService reviewBoardService;

    // ✨ POST /api/v1/reviews 요청 처리 (게시글 생성)
    @PostMapping
    public ResponseEntity<ReviewBoardResponseDto> createReview(@RequestBody @Valid ReviewBoardRequestDto requestDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginUserId = authentication.getName();

        ReviewBoardResponseDto response = reviewBoardService.createPost(loginUserId, requestDto);

        return ResponseEntity.ok(response);
    }

    // ✨ GET /api/v1/reviews/{id} 요청 처리 (게시글 단일 조회)
    @GetMapping("/{id}")
    public ResponseEntity<ReviewBoardResponseDto> getReviewById(@PathVariable Long id) {

        ReviewBoardResponseDto response = reviewBoardService.getPostById(id);

        return ResponseEntity.ok(response);
    }

    // ✨ GET /api/v1/reviews 요청 처리 (게시글 목록 조회 - Paging)
    @GetMapping
    public ResponseEntity<Page<ReviewBoardResponseDto>> getAllReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ReviewBoardResponseDto> response = reviewBoardService.getAllPosts(pageable);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ReviewBoardResponseDto> updateReview(
            @PathVariable Long id,
            @RequestBody @Valid ReviewBoardRequestDto requestDto
    ) {
        // 1. 현재 로그인 사용자(수정자)의 ID를 Security Context에서 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginUserId = authentication.getName();

        // 2. 서비스 계층으로 ID, 사용자 ID, 요청 DTO를 전달하여 수정 처리
        ReviewBoardResponseDto response = reviewBoardService.updatePost(id, loginUserId, requestDto);

        return ResponseEntity.ok(response);
    }
}