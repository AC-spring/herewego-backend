package com.example.webserver.travel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer dayNumber; // 1일차, 2일차...

    @Column(nullable = false)
    private Integer orderIndex; // 순서 0, 1, 2...

    @Column(nullable = false)
    private String placeId;     // [중요] 프론트에서 넘어온 고유 ID

    @Column(nullable = false)
    private String placeName;   // 여행지 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    public static Schedule createSchedule(Integer dayNumber, Integer orderIndex, String placeId, String placeName) {
        Schedule schedule = new Schedule();
        schedule.dayNumber = dayNumber;
        schedule.orderIndex = orderIndex;
        schedule.placeId = placeId;
        schedule.placeName = placeName;
        return schedule;
    }
}