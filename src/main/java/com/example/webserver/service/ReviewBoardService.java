package com.example.webserver.service;

import com.example.webserver.dto.request.ReviewBoardRequestDto;
import com.example.webserver.dto.response.ReviewBoardResponseDto;
import com.example.webserver.entity.ReviewBoard;
import com.example.webserver.entity.User;
import com.example.webserver.repository.ReviewBoardRepository;
import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewBoardService {

    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;

    // -----------------------------------------------------------------
    // 1. 게시글 생성 (Create)
    // -----------------------------------------------------------------
    @Transactional
    public ReviewBoardResponseDto createPost(String loginUserId, ReviewBoardRequestDto requestDto) {

        // 1. 작성자(User) 엔티티 조회
        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다. (ID: " + loginUserId + ")"));

        // 2. 게시글 엔티티 생성 (DB 컬럼 반영)
        ReviewBoard board = ReviewBoard.builder()
                .user(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .region(requestDto.getRegion())
                .spotContentId(requestDto.getSpotContentId())
                .build();

        // 3. DB 저장
        ReviewBoard savedBoard = reviewBoardRepository.save(board);

        return ReviewBoardResponseDto.of(savedBoard);
    }

    // -----------------------------------------------------------------
    // 2. 게시글 단일 조회 (Read by Id)
    // -----------------------------------------------------------------
    @Transactional
    public ReviewBoardResponseDto getPostById(Long id) {

        // 1. 게시글 조회 (없으면 예외 발생)
        ReviewBoard board = reviewBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. (ID: " + id + ")"));

        // 2. 조회수 증가 및 DB 반영
        board.incrementViewCount();

        // 3. DTO로 변환하여 반환
        return ReviewBoardResponseDto.of(board);
    }
}