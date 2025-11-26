package com.example.webserver.repository;

import com.example.webserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginUserId(String loginUserId);
    Optional<User> findByLoginUserId(String loginUserId);

    // ✨ 리프레시 토큰을 이용해 사용자를 찾는 메서드 추가
    /**
     * Refresh Token 문자열로 User 엔티티를 조회합니다.
     * 이 메서드는 클라이언트의 RT와 DB의 RT 일치성을 검증하는 데 사용됩니다.
     */
    Optional<User> findByRefreshToken(String refreshToken);
}