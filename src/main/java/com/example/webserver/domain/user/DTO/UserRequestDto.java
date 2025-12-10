package com.example.webserver.domain.user.DTO;

import com.example.webserver.domain.user.Entity.User;
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
    private String loginUserId; // 로그인 ID

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해 주세요.")
    private String password; // 비밀번호 (평문)

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 20, message = "닉네임은 20자 이하로 입력해 주세요.")
    private String nickname; // 닉네임

    /** DTO를 User 엔티티로 변환합니다. 비밀번호는 반드시 인코딩됩니다. */
    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
                .loginUserId(loginUserId)
                .passwordHash(passwordEncoder.encode(password)) // 비밀번호 암호화
                .nickname(nickname)
                .isAdmin(false) // 기본적으로 일반 사용자
                .build();
    }
}