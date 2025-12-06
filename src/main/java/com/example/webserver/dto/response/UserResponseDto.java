package com.example.webserver.dto.response;

import com.example.webserver.entity.User;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 사용자 정보 조회 응답 DTO.
 * 보안상의 이유로 비밀번호 해시 값(passwordHash)을 포함하지 않습니다.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private Long userId;
    private String loginUserId;

    // ✅ 닉네임 필드 추가
    private String nickname;

    private LocalDateTime joinDate; // 가입일 정보 유지

    /**
     * User 엔티티를 UserResponseDto로 변환하는 정적 팩토리 메서드.
     * @param user 조회된 User 엔티티
     * @return 비밀번호 해시를 제외한 UserResponseDto
     */
    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .loginUserId(user.getLoginUserId())
                .nickname(user.getNickname()) // ✅ 엔티티의 닉네임 값을 DTO에 설정
                .joinDate(user.getJoinDate())
                .build();
    }
}