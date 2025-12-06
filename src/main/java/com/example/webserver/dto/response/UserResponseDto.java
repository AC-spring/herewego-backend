package com.example.webserver.dto.response;

import com.example.webserver.entity.User;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private Long userId;
    private String loginUserId;
    private String nickname; // 닉네임 필드 추가
    private LocalDateTime joinDate;

    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .loginUserId(user.getLoginUserId())
                .nickname(user.getNickname()) // 엔티티의 닉네임 값을 DTO에 설정
                .joinDate(user.getJoinDate())
                .build();
    }
}