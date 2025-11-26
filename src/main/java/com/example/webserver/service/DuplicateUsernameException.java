package com.example.webserver.service; // AuthService와 동일 패키지

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(message);
    }
}