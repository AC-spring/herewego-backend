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
}