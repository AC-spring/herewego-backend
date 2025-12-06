package com.example.webserver.dto.response;

import com.example.webserver.entity.User;
import lombok.*;
import java.time.LocalDateTime;

/** 사용자에게 응답할 때 사용하는 DTO입니다. 보안상 비밀번호는 포함하지 않습니다. */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private Long userId;
    private String loginUserId;
    private String nickname;
    private LocalDateTime joinDate;

    /** User 엔티티를 DTO로 변환하는 정적 팩토리 메서드 */
    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .loginUserId(user.getLoginUserId())
                .nickname(user.getNickname())
                .joinDate(user.getJoinDate())
                .build();
    }
}