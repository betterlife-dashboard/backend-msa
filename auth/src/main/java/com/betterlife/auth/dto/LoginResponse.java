package com.betterlife.auth.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString(exclude = "token")
public class LoginResponse {
    private String name;
    private String email;
    private String token;

    @Builder
    public LoginResponse(String name, String email, String token) {
        this.name = name;
        this.email = email;
        this.token = token;
    }
}
