package com.example.webserver.service;

import com.example.webserver.dto.request.CommentRequestDto;
import com.example.webserver.dto.response.CommentResponseDto;
import com.example.webserver.entity.Comment;
import com.example.webserver.entity.ReviewBoard;
import com.example.webserver.entity.User;
import com.example.webserver.repository.CommentRepository;
import com.example.webserver.repository.ReviewBoardRepository;
import com.example.webserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;

    // ------------------- 댓글 작성 -------------------

    @Transactional
    public CommentResponseDto createComment(Long reviewId, String loginUserId, CommentRequestDto requestDto) {
        // 1. ReviewBoard와 User 엔티티 조회
        ReviewBoard review = reviewBoardRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 ID를 찾을 수 없습니다: " + reviewId));

        User user = userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 계정을 찾을 수 없습니다: " + loginUserId));

        // 2. Comment 엔티티 생성 및 저장
        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .review(review)
                .user(user)
                .build();

        comment = commentRepository.save(comment);

        // 3. Response DTO로 변환
        return CommentResponseDto.of(comment);
    }

    // ------------------- 댓글 조회 -------------------

    public List<CommentResponseDto> getCommentsByReviewId(Long reviewId) {
        List<Comment> comments = commentRepository.findAllByReviewIdOrderByCreatedAtAsc(reviewId);

        return comments.stream()
                .map(CommentResponseDto::of)
                .collect(Collectors.toList());
    }

    // ------------------- 댓글 수정 -------------------

    @Transactional
    public CommentResponseDto updateComment(Long commentId, String loginUserId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 ID를 찾을 수 없습니다: " + commentId));

        // 1. 권한 확인: 관리자이거나 작성자 본인이어야 수정 가능
        if (!hasPermission(comment, loginUserId)) {
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }

        // 2. 내용 업데이트
        comment.updateContent(requestDto.getContent());

        return CommentResponseDto.of(comment);
    }

    // ------------------- 댓글 삭제 -------------------

    @Transactional
    public void deleteComment(Long commentId, String loginUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 ID를 찾을 수 없습니다: " + commentId));

        // 1. 권한 확인: 관리자이거나 작성자 본인이어야 삭제 가능
        if (!hasPermission(comment, loginUserId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        // 2. 삭제 실행
        commentRepository.delete(comment);
    }

    // ------------------- 권한 확인 유틸리티 -------------------

    /**
     * 현재 사용자가 댓글의 작성자이거나 관리자인지 확인합니다.
     * @param comment 검증할 댓글 엔티티
     * @param loginUserId 현재 로그인한 사용자 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    private boolean hasPermission(Comment comment, String loginUserId) {
        // 1. 현재 사용자 엔티티를 조회하여 권한 정보 획득
        Optional<User> currentUserOpt = userRepository.findByLoginUserId(loginUserId);

        if (currentUserOpt.isEmpty()) {
            return false; // 사용자를 찾을 수 없음
        }

        User currentUser = currentUserOpt.get();

        // 2. 권한 확인 로직
        // A. 관리자 권한 확인 (ROLE_ADMIN)
        // User 엔티티의 getAuthorities()가 is_admin 필드를 기반으로 ROLE_ADMIN을 반환함
        boolean isAdmin = currentUser.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // B. 작성자 본인 확인
        boolean isOwner = comment.getUser().getLoginUserId().equals(loginUserId);

        // 관리자이거나 작성자 본인인 경우 접근 허용
        return isAdmin || isOwner;
    }
}