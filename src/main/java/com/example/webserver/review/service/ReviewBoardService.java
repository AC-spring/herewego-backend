package com.example.webserver.review.service;

import com.example.webserver.review.dto.request.ReviewBoardRequestDto;
import com.example.webserver.review.dto.response.ReviewBoardResponseDto;
import com.example.webserver.review.entity.ReviewBoard;
import com.example.webserver.auth.entity.User;
import com.example.webserver.review.repository.ReviewBoardRepository;
import com.example.webserver.auth.repository.UserRepository;
import com.example.webserver.auth.exception.AuthorizationException; // ★ 추가: Custom Exception Import
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

    // -----------------------------------------------------------------
    // ★★★ 추가: 관리자 권한 및 작성자 일치 확인 헬퍼 메서드 ★★★
    // -----------------------------------------------------------------
    /**
     * 게시글 수정/삭제 권한을 확인합니다. (작성자 또는 관리자만 허용)
     */
    private void checkAuthorization(ReviewBoard board, String loginUserId) {

        // 1. 현재 로그인 사용자 조회 (관리자 권한 확인용)
        User currentUser = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("현재 로그인된 사용자를 찾을 수 없습니다."));

        // 2. 권한 확인 로직: 작성자이거나 관리자이면 통과
        boolean isAuthor = board.getUser().getLoginUserId().equals(loginUserId);
        boolean isAdmin = currentUser.isAdmin(); // User 엔티티에 isAdmin() 메서드가 있다고 가정

        if (!isAuthor && !isAdmin) {
            // ★★★ 변경: RuntimeException 대신 AuthorizationException을 던집니다. ★★★
            throw new AuthorizationException("게시글 수정/삭제 권한이 없습니다. (작성자 또는 관리자만 가능)");
        }
    }


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

    // -----------------------------------------------------------------
    // 4. 게시글 수정 (Update) - 권한 확인 로직 호출
    // -----------------------------------------------------------------
    @Transactional
    public ReviewBoardResponseDto updatePost(Long id, String loginUserId, ReviewBoardRequestDto requestDto) {

        // 1. 게시글 조회 (없으면 예외)
        ReviewBoard board = reviewBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("수정할 게시글을 찾을 수 없습니다. (ID: " + id + ")"));

        // 2. 권한 확인 (작성자 또는 관리자)
        checkAuthorization(board, loginUserId);

        // 3. Entity 수정
        board.updatePost(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getRegion()
        );

        return ReviewBoardResponseDto.of(board);
    }

    // -----------------------------------------------------------------
    // 5. 게시글 삭제 (Delete) - 권한 확인 로직 호출
    // -----------------------------------------------------------------
    @Transactional
    public void deletePost(Long id, String loginUserId) {

        // 1. 게시글 조회 (없으면 예외)
        ReviewBoard board = reviewBoardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 게시글을 찾을 수 없습니다. (ID: " + id + ")"));

        // 2. 권한 확인 (작성자 또는 관리자)
        checkAuthorization(board, loginUserId);

        // 3. DB에서 게시글 삭제
        reviewBoardRepository.delete(board);
    }
}