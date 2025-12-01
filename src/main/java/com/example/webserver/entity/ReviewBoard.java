package com.example.webserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewBoard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // 작성자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private int viewCount;

    @Column(length = 50)
    private String region;

    @Column(name = "spot_content_id", nullable = false)
    private String spotContentId;

    @Builder
    public ReviewBoard(User user, String title, String content, String region, String spotContentId) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.region = region;
        this.spotContentId = spotContentId;
        this.viewCount = 0;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updatePost(String title, String content, String region) {
        this.title = title;
        this.content = content;
        this.region = region;
    }
}