package com.example.webserver.travel.service;

import com.example.webserver.travel.entity.Schedule;
import com.example.webserver.travel.entity.Trip;
import com.example.webserver.travel.repository.TripRepository;
import com.example.webserver.travel.dto.TripRequest;
import com.example.webserver.travel.dto.TripResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;

    // 1. 여행 생성 (Create)
    @Transactional
    public Long createTrip(TripRequest request) {
        // Trip 엔티티 생성
        Trip trip = Trip.createTrip(request.getTitle(), request.getStartDate(), request.getEndDate());

        // Schedule 엔티티 생성 및 연결
        if (request.getSchedules() != null) {
            for (TripRequest.ScheduleDto dto : request.getSchedules()) {
                Schedule schedule = Schedule.createSchedule(
                        dto.getDay(),
                        dto.getOrder(),
                        dto.getPlaceId(),
                        dto.getPlaceName()
                );
                trip.addSchedule(schedule); // 연관관계 설정
            }
        }

        Trip savedTrip = tripRepository.save(trip); // Cascade 설정으로 Schedule도 자동 저장
        return savedTrip.getId();
    }

    // 2. 여행 상세 조회 (Read)
    public TripResponse getTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 정보를 찾을 수 없습니다. id=" + tripId));

        // Entity 내의 @OrderBy 덕분에 schedules는 이미 정렬되어 있음
        return new TripResponse(trip);
    }

    // 3. 여행 수정 (Update) - 전체 교체 전략
    @Transactional
    public void updateTrip(Long tripId, TripRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 정보를 찾을 수 없습니다."));

        // A. 기본 정보 수정 (JPA 변경 감지)
        trip.setTitle(request.getTitle());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());

        // B. 스케줄 수정 (싹 비우고 다시 채우기)
        // orphanRemoval = true 덕분에 clear() 하면 DB에서 DELETE 쿼리가 나감
        trip.getSchedules().clear();

        if (request.getSchedules() != null) {
            for (TripRequest.ScheduleDto dto : request.getSchedules()) {
                Schedule schedule = Schedule.createSchedule(
                        dto.getDay(),
                        dto.getOrder(),
                        dto.getPlaceId(),
                        dto.getPlaceName()
                );
                trip.addSchedule(schedule); // 다시 추가 (INSERT 쿼리 나감)
            }
        }
    }

    // 4. 여행 삭제 (Delete)
    @Transactional
    public void deleteTrip(Long tripId) {
        // CascadeType.ALL 덕분에 Trip만 지우면 Schedule도 자동 삭제
        tripRepository.deleteById(tripId);
    }
}