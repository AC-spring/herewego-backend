package com.example.webserver.travel.controller;

import com.example.webserver.travel.dto.TripRequest;
import com.example.webserver.travel.dto.TripResponse;
import com.example.webserver.travel.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // 저장
    @PostMapping
    public ResponseEntity<Long> saveTrip(@RequestBody TripRequest request) {
        Long tripId = tripService.createTrip(request);
        return ResponseEntity.ok(tripId);
    }
    // 전체 조회
    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips() {
        List<TripResponse> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }
    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        TripResponse response = tripService.getTrip(id);
        return ResponseEntity.ok(response);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTrip(@PathVariable Long id, @RequestBody TripRequest request) {
        tripService.updateTrip(id, request);
        return ResponseEntity.ok("수정 완료");
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok("삭제 완료");
    }
}