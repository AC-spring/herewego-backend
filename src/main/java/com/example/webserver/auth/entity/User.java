package com.example.webserver.auth.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// ✨ [중요] ReviewBoard 엔티티 위치를 import 해야 합니다.
// (빨간줄 뜨면 Alt+Enter로 import 하세요)
import com.example.webserver.review.entity.ReviewBoard;

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
    private String nickname;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    // ✨ [추가된 부분] 회원이 쓴 리뷰들과 연결 (탈퇴 시 자동 삭제 설정)
    // mappedBy = "user": ReviewBoard 엔티티 안에 있는 변수 이름이 'user'여야 합니다.
    // cascade = CascadeType.ALL: 유저가 삭제되면 리뷰도 같이 삭제 (REMOVE 전파)
    // orphanRemoval = true: 연결이 끊어진 리뷰는 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewBoard> reviews = new ArrayList<>();

    @Builder
    public User(String loginUserId, String passwordHash, String nickname, boolean isAdmin) {
        this.loginUserId = loginUserId;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        this.joinDate = LocalDateTime.now();
        this.refreshToken = null;
    }

    // --- 엔티티 상태 변경 메서드 ---

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void deleteRefreshToken() {
        this.refreshToken = null;
    }

    // --- Spring Security UserDetails 구현 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (this.isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    @Override
    public String getPassword() { return this.passwordHash; }

    @Override
    public String getUsername() { return this.loginUserId; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}