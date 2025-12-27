package com.betterlife.notify.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class FcmTokenRequest {
    private String deviceType;
    private String browserType;
    private String token;
}
