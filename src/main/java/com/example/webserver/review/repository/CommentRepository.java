package com.example.webserver.review.repository;

import com.example.webserver.review.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * 특정 ReviewBoard의 모든 댓글을 생성일 기준 오름차순으로 조회합니다.
     */
    List<Comment> findAllByReviewIdOrderByCreatedAtAsc(Long reviewId);
}