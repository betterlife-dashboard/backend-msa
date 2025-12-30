package com.betterlife.notify.dto;

import com.betterlife.notify.domain.FcmToken;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FcmTokenResponse {
    private Long userId;
    private String token;
    private LocalDate updatedAt;
    private Boolean enabled;

    public static FcmTokenResponse fromEntity(FcmToken fcmToken) {
        FcmTokenResponse tokenResponse = new FcmTokenResponse();
        tokenResponse.userId = fcmToken.getUserId();
        tokenResponse.token = fcmToken.getToken();
        tokenResponse.updatedAt = fcmToken.getUpdatedAt();
        tokenResponse.enabled = fcmToken.getEnabled();
        return tokenResponse;
    }
}
