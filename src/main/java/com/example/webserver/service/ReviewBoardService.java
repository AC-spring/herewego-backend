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
}