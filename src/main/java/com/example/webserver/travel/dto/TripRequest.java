package com.example.webserver.travel.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TripRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ScheduleDto> schedules;

    @Data
    public static class ScheduleDto {
        private Integer day;
        private Integer order;
        private String placeId;   // 고유 ID
        private String placeName;
    }
}