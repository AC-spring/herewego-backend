package com.example.webserver.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_generator")
    @SequenceGenerator(
            name = "users_seq_generator",
            sequenceName = "users_user_id_seq",
            allocationSize = 1
    )
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_user_id", length = 12, unique = true, nullable = false)
    private String loginUserId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * ✅ 닉네임 필드 추가
     * DB의 UNIQUE 및 NOT NULL 제약 조건과 일치시켜야 합니다.
     * 길이는 DB 설정에 따라 (예: length = 50) 맞춰주세요.
     */
    @Column(name = "nickname", length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Builder
    public User(String loginUserId, String passwordHash, String nickname, boolean isAdmin) { // ✅ Builder 생성자에 nickname 추가
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.nickname = nickname; // ✅ 필드 초기화
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
        this.refreshToken = null;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    // --- UserDetails 구현 메서드 (변경 없음) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = this.isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.loginUserId;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}