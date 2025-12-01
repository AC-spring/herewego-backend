package com.example.webserver.service;

import com.example.webserver.dto.request.ReviewBoardRequestDto;
import com.example.webserver.dto.response.ReviewBoardResponseDto;
import com.example.webserver.entity.ReviewBoard;
import com.example.webserver.entity.User;
import com.example.webserver.repository.ReviewBoardRepository;
import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewBoardService {

    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;

    // 1. 게시글 생성 (Create)
    @Transactional
    public ReviewBoardResponseDto createPost(String loginUserId, ReviewBoardRequestDto requestDto) {

        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다. (ID: " + loginUserId + ")"));

        ReviewBoard board = ReviewBoard.builder()
                .user(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .region(requestDto.getRegion())
                .spotContentId(requestDto.getSpotContentId())
                .build();

        ReviewBoard savedBoard = reviewBoardRepository.save(board);

        return ReviewBoardResponseDto.of(savedBoard);
    }

    // 2. 게시글 단일 조회 (Read by Id)
    @Transactional
    public ReviewBoardResponseDto getPostById(Long id) {

        ReviewBoard board = reviewBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. (ID: " + id + ")"));

        board.incrementViewCount();

        return ReviewBoardResponseDto.of(board);
    }

    // 3. 게시글 목록 조회 (Read List with Paging)
    @Transactional(readOnly = true)
    public Page<ReviewBoardResponseDto> getAllPosts(Pageable pageable) {

        Page<ReviewBoard> boardPage = reviewBoardRepository.findAll(pageable);

        return boardPage.map(ReviewBoardResponseDto::of);
    }
    @Transactional // 수정 작업이므로 @Transactional 필수
    public ReviewBoardResponseDto updatePost(Long id, String loginUserId, ReviewBoardRequestDto requestDto) {

        // 1. 게시글 조회 (없으면 예외)
        ReviewBoard board = reviewBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("수정할 게시글을 찾을 수 없습니다. (ID: " + id + ")"));

        // 2. ★★★ 권한 확인 (현재 로그인 ID와 작성자 ID 비교) ★★★
        if (!board.getUser().getLoginUserId().equals(loginUserId)) {
            // 실제 구현에서는 Custom Exception (403 Forbidden)을 던지는 것이 좋습니다.
            throw new RuntimeException("게시글 수정 권한이 없습니다. (작성자 불일치)");
        }

        // 3. Entity 수정 (ReviewBoard.java의 updatePost 메서드 사용)
        board.updatePost(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getRegion()
        );

        // @Transactional에 의해 수정 내용과 updatedAt이 자동 반영됨

        // 4. DTO로 변환하여 반환
        return ReviewBoardResponseDto.of(board);
    }
}