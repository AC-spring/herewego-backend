package com.example.webserver.repository;

import com.example.webserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 기존 메서드
    boolean existsByLoginUserId(String loginUserId);
    Optional<User> findByLoginUserId(String loginUserId);

    // 리프레시 토큰 관련 메서드
    Optional<User> findByRefreshToken(String refreshToken);

    //닉네임
    boolean existsByNickname(String nickname);
}