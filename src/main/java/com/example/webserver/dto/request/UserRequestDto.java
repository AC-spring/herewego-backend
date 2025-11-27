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

    public User toUser(PasswordEncoder passwordEncoder) {
        return User.builder()
                .loginUserId(loginUserId)
                .passwordHash(passwordEncoder.encode(password))
                .isAdmin(false)
                .build();
    }
}