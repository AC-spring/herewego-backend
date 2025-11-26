package com.example.webserver.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_generator")
    @SequenceGenerator(
            name = "users_seq_generator",
            sequenceName = "users_user_id_seq",
            allocationSize = 1
    )
    @Column(name = "user_id") // <<< DB 스키마에 맞춰 소문자 'user_id'로 최종 확정
    private Long userId;

    @Column(name = "login_user_id", length = 12, unique = true, nullable = false) // loginUserId로 변경 및 길이 제한
    private String loginUserId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Builder
    public User(String loginUserId, String passwordHash, boolean isAdmin) {
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
    }
}