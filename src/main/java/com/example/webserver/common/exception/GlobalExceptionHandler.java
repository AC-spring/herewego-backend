package com.example.webserver.common.exception; // 같은 exception 패키지에 위치한다고 가정

import com.example.webserver.auth.exception.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 권한 없음 (403 Forbidden) 예외 처리
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Map<String, String>> handleAuthorizationException(AuthorizationException ex) {

        // 403 상태 코드와 에러 메시지를 JSON 형태로 반환
        return ResponseEntity.status(HttpStatus.FORBIDDEN) // HTTP 403
                .body(Map.of("error", "Forbidden", "message", ex.getMessage()));
    }

    /**
     * 리소스를 찾을 수 없음 (404 Not Found) 예외 처리
     */
    @ExceptionHandler(RuntimeException.class) // 기존 RuntimeException도 처리
    public ResponseEntity<Map<String, String>> handleNotFoundException(RuntimeException ex) {
        // 기존 코드에서 "게시글을 찾을 수 없습니다."와 같은 메시지를 처리하는 예외
        if (ex.getMessage().contains("찾을 수 없습니다.")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) // HTTP 404
                    .body(Map.of("error", "Not Found", "message", ex.getMessage()));
        }

        // 그 외의 처리되지 않은 RuntimeException은 500을 반환 (기존 동작 유지)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error", "message", ex.getMessage()));
    }
}