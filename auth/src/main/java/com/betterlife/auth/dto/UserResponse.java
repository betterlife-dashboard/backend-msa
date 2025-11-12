package com.betterlife.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserResponse {
    private String email;
    private String name;

    @Builder
    public UserResponse(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
