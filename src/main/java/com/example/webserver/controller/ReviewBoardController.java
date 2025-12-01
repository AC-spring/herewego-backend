package com.example.webserver.controller;

import com.example.webserver.dto.request.ReviewBoardRequestDto;
import com.example.webserver.dto.response.ReviewBoardResponseDto;
import com.example.webserver.dto.ResponseWrapperDto; // 메시지와 데이터를 래핑하는 DTO
import com.example.webserver.service.ReviewBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    /**
     * 현재 로그인된 사용자(Principal)의 ID를 SecurityContext에서 가져오는 헬퍼 메서드
     */
    private String getLoginUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Principal(로그인 ID) 반환
    }

    // -----------------------------------------------------------------
    // 1. POST /api/v1/reviews 요청 처리 (게시글 생성)
    // -----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ResponseWrapperDto<ReviewBoardResponseDto>> createReview(
            @RequestBody @Valid ReviewBoardRequestDto requestDto
    ) {
        String loginUserId = getLoginUserId();

        ReviewBoardResponseDto responseData = reviewBoardService.createPost(loginUserId, requestDto);

        // 성공 메시지와 함께 데이터를 래핑하여 반환
        ResponseWrapperDto<ReviewBoardResponseDto> response = ResponseWrapperDto.success(
                "게시글이 성공적으로 작성되었습니다.",
                responseData
        );
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------
    // 2. GET /api/v1/reviews/{id} 요청 처리 (게시글 단일 조회)
    // -----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ReviewBoardResponseDto> getReviewById(@PathVariable Long id) {

        // 단일 조회는 ResponseWrapper 없이 원본 DTO를 반환하는 것이 일반적입니다.
        ReviewBoardResponseDto response = reviewBoardService.getPostById(id);

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------
    // 3. GET /api/v1/reviews 요청 처리 (게시글 목록 조회)
    // -----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<ReviewBoardResponseDto>> getAllReviews(
            // 페이지당 10개, createdAt 기준으로 내림차순 정렬을 기본값으로 설정
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewBoardResponseDto> response = reviewBoardService.getAllPosts(pageable);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------
    // 4. PUT /api/v1/reviews/{id} 요청 처리 (게시글 수정)
    // -----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapperDto<ReviewBoardResponseDto>> updateReview(
            @PathVariable Long id,
            @RequestBody @Valid ReviewBoardRequestDto requestDto
    ) {
        String loginUserId = getLoginUserId();

        // 권한 확인 및 수정 처리
        ReviewBoardResponseDto responseData = reviewBoardService.updatePost(id, loginUserId, requestDto);

        // 성공 메시지와 함께 데이터를 래핑하여 반환
        ResponseWrapperDto<ReviewBoardResponseDto> response = ResponseWrapperDto.success(
                "게시글이 성공적으로 수정되었습니다.",
                responseData
        );
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------
    // 5. DELETE /api/v1/reviews/{id} 요청 처리 (게시글 삭제)
    // -----------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapperDto<Void>> deleteReview(@PathVariable Long id) {

        String loginUserId = getLoginUserId();

        // 권한 확인 및 삭제 처리
        reviewBoardService.deletePost(id, loginUserId);

        // 데이터(Void) 없이 성공 메시지만 반환 (HTTP 200 OK)
        return ResponseEntity.ok(ResponseWrapperDto.success("게시글이 성공적으로 삭제되었습니다."));
    }
}