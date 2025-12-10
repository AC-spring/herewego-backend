package com.example.webserver.travel.repository;

import com.example.webserver.travel.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
    // 기본 CRUD 메서드는 JpaRepository가 제공하므로 작성 불필요
    // 복잡한 쿼리가 필요할 때만 @Query 작성
}