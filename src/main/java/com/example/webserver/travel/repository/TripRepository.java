package com.example.webserver.travel.repository;

import com.example.webserver.travel.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // "User의 UserId가 일치하는 Trip을 찾아라"
    // (SQL: SELECT * FROM trips WHERE user_id = ?)
    List<Trip> findAllByUser_UserIdOrderByStartDateDesc(Long userId);
}