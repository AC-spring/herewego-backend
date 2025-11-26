package com.example.webserver.repository;

import com.example.webserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginUserId(String loginUserId);
    Optional<User> findByLoginUserId(String loginUserId);
}