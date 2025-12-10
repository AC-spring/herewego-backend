package com.example.webserver.review.repository;

import com.example.webserver.review.entity.ReviewBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewBoardRepository extends JpaRepository<ReviewBoard, Long> {
    // ... 필요한 추가 메서드 (예: findByTitleContaining 등)
}