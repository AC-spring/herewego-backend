package com.example.webserver.travel.entity;

import com.example.webserver.auth.entity.User; // ✨ User 엔티티 임포트 필수!
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

    // ✨ [중요] 작성자(User)와 연결하는 필드 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // DB에 user_id 컬럼이 생깁니다.
    private User user;

    // 양방향 연관관계 설정
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC, orderIndex ASC")
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