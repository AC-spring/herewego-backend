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

    @Column(name = "nickname", length = 50, unique = true, nullable = false)
    private String nickname; // 닉네임 필드 추가

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Builder
    public User(String loginUserId, String passwordHash, String nickname, boolean isAdmin) {
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
        this.refreshToken = null;
    }

    // 마이페이지 기능 구현을 위한 수정 메서드
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    // 리프레시 토큰 관리 메서드
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    // --- UserDetails 구현 메서드 ---

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