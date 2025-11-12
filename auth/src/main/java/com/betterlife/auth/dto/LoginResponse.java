package com.betterlife.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LoginResponse {
    private String token;

    @Builder
    public LoginResponse(String token) {
        this.token = token;
    }
}
