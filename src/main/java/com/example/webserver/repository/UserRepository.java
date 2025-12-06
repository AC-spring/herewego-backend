package com.example.webserver.repository;

import com.example.webserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 로그인 ID가 존재하는지 확인 (회원가입 시 중복 확인에 사용) */
    boolean existsByLoginUserId(String loginUserId);

    /** 로그인 ID로 사용자 엔티티를 조회합니다. (인증 및 정보 조회에 사용) */
    Optional<User> findByLoginUserId(String loginUserId);

    /** Refresh Token으로 사용자 엔티티를 조회합니다. (토큰 재발급에 사용) */
    Optional<User> findByRefreshToken(String refreshToken);

    /** 닉네임이 이미 존재하는지 확인합니다. (회원가입 및 닉네임 변경에 사용) */
    boolean existsByNickname(String nickname);
}