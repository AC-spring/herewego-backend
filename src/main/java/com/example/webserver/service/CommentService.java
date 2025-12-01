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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;

    // ------------------- DTO 정의 -------------------
    // (DTO 파일은 별도로 존재한다고 가정)

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

        // 1. 권한 확인: 현재 로그인 사용자와 댓글 작성자가 동일해야 수정 가능
        if (!comment.getUser().getLoginUserId().equals(loginUserId)) {
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

        // 1. 권한 확인: 현재 로그인 사용자와 댓글 작성자가 동일해야 삭제 가능
        if (!comment.getUser().getLoginUserId().equals(loginUserId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        // 2. 삭제 실행
        commentRepository.delete(comment);
    }
}