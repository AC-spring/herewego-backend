package com.example.webserver.travel.service;

import com.example.webserver.auth.entity.User;
import com.example.webserver.auth.repository.UserRepository;
import com.example.webserver.travel.entity.Schedule;
import com.example.webserver.travel.entity.Trip;
import com.example.webserver.travel.repository.TripRepository;
import com.example.webserver.travel.dto.TripRequest;
import com.example.webserver.travel.dto.TripResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * 유저의 로그인 ID(String)를 받아서 User 엔티티를 조회하는 헬퍼 메서드
     * 이 메서드를 통해 토큰(String ID)에서 DB 엔티티(User)를 연결합니다.
     */
    private User findUserByLoginId(String loginUserId) {
        return userRepository.findByLoginUserId(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자(loginId: " + loginUserId + ")를 찾을 수 없습니다."));
    }

    // 1. 여행 생성 (Create) - 작성자 저장 포함
    @Transactional
    public Long createTrip(TripRequest request, String loginUserId) { // ✨ 파라미터 타입 변경
        // ✨ 유저 엔티티 조회 (DB 쿼리 발생)
        User user = findUserByLoginId(loginUserId);

        // Trip 엔티티 생성
        Trip trip = Trip.createTrip(request.getTitle(), request.getStartDate(), request.getEndDate());
        trip.setUser(user); // 작성자 설정

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

        Trip savedTrip = tripRepository.save(trip);
        return savedTrip.getId();
    }

    // 2. 내 여행 목록 조회 (Read) - String loginUserId로 필터링
    public List<TripResponse> getMyTrips(String loginUserId) { // ✨ 파라미터 타입 변경
        // ✨ 유저 엔티티 조회 (User PK를 얻기 위함)
        User user = findUserByLoginId(loginUserId);

        // PK(userId)를 사용해 내 여행만 조회
        List<Trip> trips = tripRepository.findAllByUser_UserIdOrderByStartDateDesc(user.getUserId());

        return trips.stream()
                .map(TripResponse::new)
                .collect(Collectors.toList());
    }

    // 3. 여행 상세 조회 (Read) - 변경 없음
    public TripResponse getTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 정보를 찾을 수 없습니다. id=" + tripId));
        return new TripResponse(trip);
    }

    // 4. 여행 수정 (Update) - 권한 체크 추가
    @Transactional
    public void updateTrip(Long tripId, TripRequest request, String loginUserId) { // ✨ 파라미터 타입 변경
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 정보를 찾을 수 없습니다."));

        // ✨ 권한 확인: 작성자의 PK와 로그인한 사용자의 PK가 같은지 체크
        User currentUser = findUserByLoginId(loginUserId);
        if (!trip.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // A. 기본 정보 수정
        trip.setTitle(request.getTitle());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());

        // B. 스케줄 수정 (전체 교체 전략)
        trip.getSchedules().clear();

        if (request.getSchedules() != null) {
            for (TripRequest.ScheduleDto dto : request.getSchedules()) {
                Schedule schedule = Schedule.createSchedule(
                        dto.getDay(),
                        dto.getOrder(),
                        dto.getPlaceId(),
                        dto.getPlaceName()
                );
                trip.addSchedule(schedule);
            }
        }
    }

    // 5. 여행 삭제 (Delete) - 권한 체크 추가
    @Transactional
    public void deleteTrip(Long tripId, String loginUserId) { // ✨ 파라미터 타입 변경
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 정보를 찾을 수 없습니다."));

        // ✨ 권한 확인: 작성자의 PK와 로그인한 사용자의 PK가 같은지 체크
        User currentUser = findUserByLoginId(loginUserId);
        if (!trip.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        tripRepository.delete(trip);
    }
}