package com.example.webserver.domain.auth.DTO;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "사용자 ID를 입력해 주세요.")
    private String loginUserId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;
}