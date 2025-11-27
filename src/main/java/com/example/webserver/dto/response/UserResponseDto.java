package com.example.webserver.dto.response;

import com.example.webserver.entity.User;
import lombok.*;
import java.time.LocalDateTime; // LocalDateTime을 사용하기 위해 import 추가

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private Long userId;
    private String loginUserId;
    private String passwordHash; // <<< 고객 요청에 따라 다시 추가 (암호화된 해시 값)

    private LocalDateTime joinDate; // 가입일 정보 유지

    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .loginUserId(user.getLoginUserId())
                .passwordHash(user.getPasswordHash()) // <<< 해시된 값 전달 로직 복구
                .joinDate(user.getJoinDate())
                .build();
    }
}