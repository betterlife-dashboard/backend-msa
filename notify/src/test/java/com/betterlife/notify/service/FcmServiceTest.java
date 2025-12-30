package com.betterlife.notify.service;

import com.betterlife.notify.domain.FcmToken;
import com.betterlife.notify.dto.WebNotify;
import com.betterlife.notify.enums.DeviceType;
import com.betterlife.notify.enums.NotifyType;
import com.betterlife.notify.repository.FcmTokenRepository;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FcmServiceTest {
    @InjectMocks
    private FcmService fcmService;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GoogleCredentials googleCredentials;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Test
    void sendToFcm_withTokenError() throws Exception {
        FcmToken fcmToken = FcmToken.builder()
                .userId(3L)
                .token("01234567890123456789")
                .deviceType(DeviceType.WEB)
                .browserType("chrome")
                .updatedAt(LocalDate.now())
                .enabled(true)
                .build();
        WebNotify webNotify = WebNotify.builder()
                .userId(3L)
                .notifyType(NotifyType.REMINDER)
                .title("title")
                .body("body")
                .sendAt(LocalDateTime.parse("2025-01-01T10:00:00"))
                .todoId(9L)
                .remainTime("1h")
                .link("link")
                .build();
        when(fcmTokenRepository.findByUserIdAndEnabled(3L, true)).thenReturn(List.of(fcmToken));
        AccessToken accessToken = mock(AccessToken.class);
        when(googleCredentials.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("access-token");

        String body = "{\"error\":{\"status\":\"NOT_FOUND\",\"message\":\"UNREGISTERED\"}}";
        RestClientResponseException ex = new RestClientResponseException(
                "Not Found",
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.exchange(
                eq("https://fcm.googleapis.com/v1/projects/betterlifeboard/messages:send"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(ex);

        fcmService.sendToFcm(webNotify);

        assertThat(fcmToken.getEnabled()).isFalse();
        verify(fcmTokenRepository).save(fcmToken);
    }
}
