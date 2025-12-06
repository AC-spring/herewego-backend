package com.example.webserver.dto.request;

import com.example.webserver.entity.User;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "사용자 ID를 입력해 주세요.")
    @Size(max = 12, message = "사용자 ID는 12자 이하로 입력해 주세요.")
    private String loginUserId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해 주세요.")
    private String password;

    /**
     * ✅ 닉네임 필드 추가 및 유효성 검증
     */
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 20, message = "닉네임은 20자 이하로 입력해 주세요.") // 20자는 예시, DB 컬럼 길이에 맞게 설정
    private String nickname; // 닉네임 필드 추가

    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
                .loginUserId(loginUserId)
                .passwordHash(passwordEncoder.encode(password))
                .nickname(nickname) // ✅ User 엔티티에 닉네임 값 설정
                .isAdmin(false)
                .build();
    }
}