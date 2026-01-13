package com.betterlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class RegisterRequest {

    @Schema(example = "test")
    @NotBlank
    private String name;

    @Schema(example = "sample@test.com")
    @NotBlank @Email
    private String email;

    @Schema(example = "AbCd1234")
    @NotBlank @Size(min=8, max=72)
    private String password;
}
