package com.example.webserver.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List; // Collection 및 List 임포트 추가
import lombok.AccessLevel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 임포트

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

    @Builder
    public User(String loginUserId, String passwordHash, boolean isAdmin) {
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
    }

    // ------------------------------------------------------------------
    // 💡 UserDetails 인터페이스 필수 구현 메서드
    // ------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // is_admin 필드에 따라 권한을 부여합니다.
        // Spring Security의 권한 이름은 보통 "ROLE_" 접두사를 사용합니다.
        String role = this.isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        // DB에 저장된 해시된 비밀번호를 반환합니다.
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        // 사용자를 식별할 수 있는 고유값 (여기서는 login_user_id)을 반환합니다.
        return this.loginUserId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}