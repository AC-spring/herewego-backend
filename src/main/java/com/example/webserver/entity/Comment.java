package com.example.webserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    // ------------------- 관계 설정 -------------------

    // N:1 관계: 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // N:1 관계: 댓글이 달린 리뷰 (ReviewBoard 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false) // 컬럼명 review_id
    private ReviewBoard review;

    // ------------------- 명시적 Getter (Lombok 오류 회피용) -------------------
    public User getUser() {
        return user;
    }
    // ------------------------------------------------------------------

    @Builder
    public Comment(String content, User user, ReviewBoard review) {
        this.content = content;
        this.user = user;
        this.review = review;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}