package com.example.webserver.travel.controller;

// import com.example.webserver.auth.entity.User; // ✨ 캐스팅 오류 방지를 위해 삭제
import com.example.webserver.travel.dto.TripRequest;
import com.example.webserver.travel.dto.TripResponse;
import com.example.webserver.travel.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * 현재 로그인된 사용자의 ID (loginUserId, String)를 추출하는 헬퍼 메서드
     */
    private String getLoginUserId(UserDetails userDetails) {
        // UserDetails 객체에서 문자열 ID를 안전하게 추출합니다.
        return userDetails.getUsername();
    }

    // 저장 (내 여행으로 저장)
    @PostMapping
    public ResponseEntity<Long> saveTrip(
            @RequestBody TripRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // ✨ 문자열 ID 추출 (캐스팅 위험 없음)
        String loginUserId = getLoginUserId(userDetails);

        // 서비스에 String ID 전달
        Long tripId = tripService.createTrip(request, loginUserId);
        return ResponseEntity.ok(tripId);
    }

    // 내 여행 전체 조회 (남의 것 안 뜸)
    @GetMapping
    public ResponseEntity<List<TripResponse>> getMyTrips(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // ✨ 문자열 ID 추출
        String loginUserId = getLoginUserId(userDetails);

        // Service에서 String ID를 받아 DB에서 사용자 PK를 찾아 조회
        List<TripResponse> trips = tripService.getMyTrips(loginUserId);
        return ResponseEntity.ok(trips);
    }

    // 상세 조회 (권한 체크를 Service에서만 하도록 단순화)
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        TripResponse response = tripService.getTrip(id);
        return ResponseEntity.ok(response);
    }

    // 수정 (권한 체크를 위해 String ID 전달)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTrip(
            @PathVariable Long id,
            @RequestBody TripRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginUserId = getLoginUserId(userDetails);
        // Service 메서드 시그니처 변경 필요: updateTrip(Long id, TripRequest request, String loginUserId)
        tripService.updateTrip(id, request, loginUserId);
        return ResponseEntity.ok("수정 완료");
    }

    // 삭제 (권한 체크를 위해 String ID 전달)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginUserId = getLoginUserId(userDetails);
        // Service 메서드 시그니처 변경 필요: deleteTrip(Long id, String loginUserId)
        tripService.deleteTrip(id, loginUserId);
        return ResponseEntity.ok("삭제 완료");
    }
}