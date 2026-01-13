package com.betterlife.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ErrorMessageDto {
    private String message;

    public ErrorMessageDto(String message) {
        this.message = message;
    }
}
