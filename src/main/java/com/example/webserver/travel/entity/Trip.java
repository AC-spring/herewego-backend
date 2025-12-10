package com.example.webserver.travel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    // 양방향 연관관계 설정
    // orphanRemoval = true: 리스트에서 제거되면 DB에서도 삭제됨 (Update 로직 핵심)
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC, orderIndex ASC") // 조회 시 자동으로 순서 정렬
    private List<Schedule> schedules = new ArrayList<>();

    // 생성자 메서드
    public static Trip createTrip(String title, LocalDate startDate, LocalDate endDate) {
        Trip trip = new Trip();
        trip.title = title;
        trip.startDate = startDate;
        trip.endDate = endDate;
        return trip;
    }

    // 연관관계 편의 메서드 (스케줄 추가)
    public void addSchedule(Schedule schedule) {
        this.schedules.add(schedule);
        schedule.setTrip(this);
    }
}