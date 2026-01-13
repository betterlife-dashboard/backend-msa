package com.betterlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "password")
public class LoginRequest {

    @Schema(example = "sample@test.com")
    @NotBlank @Email
    private String email;

    @Schema(example = "AbCd1234")
    private String password;
}
