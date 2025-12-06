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
    private Long userId; // 기본 키 (DB: user_id)

    @Column(name = "login_user_id", length = 12, unique = true, nullable = false)
    private String loginUserId; // 로그인 ID (DB: login_user_id, UNIQUE)

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // 암호화된 비밀번호

    @Column(name = "nickname", length = 50, unique = true, nullable = false)
    private String nickname; // 사용자 닉네임 (DB: nickname, UNIQUE)

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate; // 가입일

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin; // 관리자 권한 여부 (true: ROLE_ADMIN)

    @Column(name = "refresh_token", length = 512)
    private String refreshToken; // JWT Refresh Token 저장 필드 (로그아웃, 재발급에 사용)

    @Builder
    public User(String loginUserId, String passwordHash, String nickname, boolean isAdmin) {
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
        this.refreshToken = null;
    }

    // --- 엔티티 상태 변경 메서드 (마이페이지 및 관리자 기능) ---

    /** 닉네임을 변경합니다. */
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /** 암호화된 비밀번호 해시를 변경합니다. */
    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /** 리프레시 토큰 값을 업데이트합니다. (로그인 및 재발급 시 사용) */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /** 리프레시 토큰 값을 NULL로 만들어 세션을 무효화합니다. (로그아웃 및 강제 로그아웃에 사용) */
    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    // --- Spring Security UserDetails 구현 ---

    /** 사용자에게 부여된 권한(ROLE) 목록을 반환합니다. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // isAdmin 값에 따라 ROLE_ADMIN 또는 ROLE_USER를 부여
        String role = this.isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // 암호화된 비밀번호를 반환
    }

    @Override
    public String getUsername() {
        return this.loginUserId; // 사용자 식별자(로그인 ID)를 반환
    }

    // 이하 메서드들은 계정 만료/잠금/활성화 여부를 반환하며, 모두 true로 설정되어 있음
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}