package com.example.webserver.entity;

import com.example.webserver.domain.user.Entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewBoard extends BaseTimeEntity { // BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // 1:N 관계: 하나의 ReviewBoard에 여러 개의 Comment가 달림
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // N:1 관계: 작성자
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