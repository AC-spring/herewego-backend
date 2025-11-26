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
public class User implements UserDetails { // 💡 UserDetails 인터페이스 구현

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

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    // ------------------------------------------------------------------
    // ✨ 리프레시 토큰 저장을 위한 필드 추가
    // ------------------------------------------------------------------
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;


    @Builder
    public User(String loginUserId, String passwordHash, boolean isAdmin) {
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
        // 회원가입 시에는 Refresh Token이 null 상태입니다.
        this.refreshToken = null;
    }

    // ------------------------------------------------------------------
    // ✨ Refresh Token 업데이트/삭제 메서드 추가
    // ------------------------------------------------------------------

    /**
     * 로그인 성공 시 새로운 Refresh Token을 저장합니다.
     * @param refreshToken 새로 발급된 Refresh Token 문자열
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 로그아웃 시 Refresh Token을 DB에서 삭제합니다 (NULL 처리).
     */
    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    // ------------------------------------------------------------------
    // UserDetails 인터페이스 필수 구현 메서드 (변경 없음)
    // ------------------------------------------------------------------

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
        return this.loginUserId; // login_user_id를 사용자 이름으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}