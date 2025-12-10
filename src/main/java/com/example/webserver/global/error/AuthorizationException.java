package com.example.webserver.global.error; // 새로운 패키지 (exception)에 위치한다고 가정

public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}