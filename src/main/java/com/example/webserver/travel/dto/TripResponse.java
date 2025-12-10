package com.example.webserver.travel.dto;

import com.example.webserver.travel.entity.Schedule;
import com.example.webserver.travel.entity.Trip;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TripResponse {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ScheduleResponse> schedules;

    // Entity -> DTO 변환 생성자
    public TripResponse(Trip trip) {
        this.id = trip.getId();
        this.title = trip.getTitle();
        this.startDate = trip.getStartDate();
        this.endDate = trip.getEndDate();
        this.schedules = trip.getSchedules().stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
    }

    @Data
    static class ScheduleResponse {
        private Integer day;
        private Integer order;
        private String placeId;
        private String placeName;

        public ScheduleResponse(Schedule schedule) {
            this.day = schedule.getDayNumber();
            this.order = schedule.getOrderIndex();
            this.placeId = schedule.getPlaceId();
            this.placeName = schedule.getPlaceName();
        }
    }
}