package com.example.webserver.repository;

import com.example.webserver.entity.ReviewBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewBoardRepository extends JpaRepository<ReviewBoard, Long> {
    // 기본 CRUD 메서드는 상속받아 사용합니다.
}