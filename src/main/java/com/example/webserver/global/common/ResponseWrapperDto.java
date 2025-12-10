package com.example.webserver.global.common; // dto 패키지에 위치

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 응답의 구조를 통일하기 위한 래퍼 DTO (메시지와 데이터를 포함)
 */
@Getter
@RequiredArgsConstructor
public class ResponseWrapperDto<T> {

    private final String message;
    private final T data; // 제네릭 타입으로 실제 응답 데이터 (DTO)를 담습니다.

    // 성공 메시지와 데이터를 함께 반환하는 팩토리 메서드
    public static <T> ResponseWrapperDto<T> success(String message, T data) {
        return new ResponseWrapperDto<>(message, data);
    }

    // 데이터 없이 메시지만 반환하는 팩토리 메서드 (삭제 시 유용)
    public static <T> ResponseWrapperDto<T> success(String message) {
        return new ResponseWrapperDto<>(message, null);
    }
}