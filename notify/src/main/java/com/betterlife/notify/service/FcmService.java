package com.betterlife.notify.service;

import com.betterlife.notify.domain.FcmToken;
import com.betterlife.notify.dto.FcmTokenRequest;
import com.betterlife.notify.dto.FcmTokenResponse;
import com.betterlife.notify.dto.WebMessageDto;
import com.betterlife.notify.dto.WebNotify;
import com.betterlife.notify.enums.DeviceType;
import com.betterlife.notify.repository.FcmTokenRepository;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final GoogleCredentials googleCredentials;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_KEY = "notify:schedule";
    private static final String TODO_INDEX_KEY = "notify:index:todo:";
    private static final String USER_INDEX_KEY = "notify:index:user:";

    public FcmTokenResponse getFcmToken(Long id, String deviceType, String browserType) {
        FcmToken fcmToken = fcmTokenRepository.findByUserIdAndDeviceTypeAndBrowserType(id, DeviceType.fromString(deviceType), browserType)
                .orElseThrow(() -> new EntityNotFoundException("해당 채널용 FCM 토큰이 아직 발급되지 않았습니다."));
        return FcmTokenResponse.fromEntity(fcmToken);
    }

    public FcmTokenResponse saveFcmToken(Long id, FcmTokenRequest fcmTokenRequest) {
        FcmToken fcmToken = FcmToken.builder()
                .userId(id)
                .token(fcmTokenRequest.getToken())
                .deviceType(DeviceType.fromString(fcmTokenRequest.getDeviceType()))
                .browserType(fcmTokenRequest.getBrowserType())
                .updatedAt(LocalDate.now())
                .enabled(true)
                .build();
        FcmToken saved = fcmTokenRepository.save(fcmToken);
        return FcmTokenResponse.fromEntity(saved);
    }

    public void popDueNotify() throws Exception {
        long now = System.currentTimeMillis();

        Set<String> notifyIds = redisTemplate.opsForZSet()
                .rangeByScore(QUEUE_KEY, 0, now);

        if (notifyIds == null || notifyIds.isEmpty()) return;

        for (String notifyId : notifyIds) {
            String job = redisTemplate.opsForValue().get("notify:" + notifyId);
            WebNotify webNotify = objectMapper.readValue(job, WebNotify.class);
            sendToFcm(webNotify);
            redisTemplate.delete("notify:" + notifyId);
            redisTemplate.opsForZSet().remove(QUEUE_KEY, notifyId);
            redisTemplate.opsForSet().remove(USER_INDEX_KEY + webNotify.getUserId(), notifyId);
            redisTemplate.opsForSet().remove(TODO_INDEX_KEY + webNotify.getTodoId(), notifyId);
        }
    }

    public void sendToFcm(WebNotify webNotify) throws Exception {
        List<FcmToken> fcmTokenList = fcmTokenRepository.findByUserIdAndEnabled(webNotify.getUserId(), true);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        googleCredentials.refreshIfExpired();
        headers.set("Authorization", "Bearer " + googleCredentials.getAccessToken().getTokenValue());
        for (FcmToken fcmToken : fcmTokenList) {
            WebMessageDto webMessageDto = WebMessageDto.builder()
                    .message(WebMessageDto.Message.builder()
                            .token(fcmToken.getToken())
                            .data(Map.of(
                                    "title", webNotify.getTitle(),
                                    "body", webNotify.getBody(),
                                    "link", webNotify.getLink()))
                            .webpush(WebMessageDto.Webpush.builder()
                                    .headers(Map.of("TTL", "60"))
                                    .build())
                            .build())
                    .build();
            HttpEntity<WebMessageDto> entity = new HttpEntity<>(webMessageDto, headers);
            String API_URL = "https://fcm.googleapis.com/v1/projects/betterlifeboard/messages:send";
            try {
                ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException("FCM 전송 실패: " + response.getStatusCode());
                }
            } catch (RestClientResponseException ex) {
                String body = ex.getResponseBodyAsString();
                if (ex.getStatusCode().value() == 404 && body != null && body.contains("UNREGISTERED")) {
                    fcmToken.disable();
                    fcmTokenRepository.save(fcmToken);
                    log.warn("FCM 토큰 비활성화 처리: {}", fcmToken.getToken());
                    continue;
                }
                throw ex;
            }
        }
    }
}
