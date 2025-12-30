package com.betterlife.notify.service;

import com.betterlife.notify.dto.WebNotify;
import com.betterlife.notify.enums.NotifyType;
import com.betterlife.notify.enums.RemainTimeRule;
import com.betterlife.notify.event.ScheduleCreatedEvent;
import com.betterlife.notify.event.ScheduleDeletedEvent;
import com.betterlife.notify.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_KEY = "notify:schedule";
    private static final String TODO_INDEX_KEY = "notify:index:todo:";
    private static final String USER_INDEX_KEY = "notify:index:user:";

    public void createScheduleNotify(ScheduleCreatedEvent event) {
        WebNotify webNotify = toScheduleNotify(event);
        try {
            String notifyId = UUID.randomUUID().toString();
            String key = "notify:" + notifyId;
            String json = objectMapper.writeValueAsString(webNotify);
            long score = webNotify.getSendAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            redisTemplate.opsForValue().set(key, json);
            redisTemplate.opsForZSet().add(QUEUE_KEY, notifyId, score);
            redisTemplate.opsForSet().add(TODO_INDEX_KEY + webNotify.getTodoId(), notifyId);
            redisTemplate.opsForSet().add(USER_INDEX_KEY + webNotify.getUserId(), notifyId);
        } catch (Exception e) {
            throw new RuntimeException("Redis 저장 실패", e);
        }
    }

    private WebNotify toScheduleNotify(ScheduleCreatedEvent event) {
        NotifyType notifyType = NotifyType.from(event.getStandard());
        RemainTimeRule rule = RemainTimeRule.fromCode(event.getRemainTime());
        String remainTime = rule.getLabel();
        LocalDateTime sendAt = rule.apply(LocalDateTime.parse(event.getDeadlineTime()));
        String title = notifyType.renderTitle(
                Map.of("title", event.getTitle())
        );
        String body = notifyType.renderBody(
                Map.of(
                        "title", event.getTitle(),
                        "timeLeft", remainTime
                )
        );
        return WebNotify.builder()
                .userId(event.getUserId())
                .notifyType(notifyType)
                .title(title)
                .body(body)
                .sendAt(sendAt)
                .todoId(event.getTodoId())
                .remainTime(event.getRemainTime())
                .link("https://www.betterlife.betterlifeboard.com")
                .build();
    }

    public List<WebNotify> getNotifies(Long todoId) {
        Set<String> notifyIds = redisTemplate.opsForSet().members(TODO_INDEX_KEY + todoId);

        if (notifyIds == null || notifyIds.isEmpty()) {
            return List.of();
        }


        return notifyIds.stream().map(notifyId -> {
            String json = redisTemplate.opsForValue().get("notify:" + notifyId);
            return objectMapper.readValue(json, WebNotify.class);
        }).toList();
    }

    public void deleteScheduleNotify(ScheduleDeletedEvent event) {
        Set<String> notifyIds = redisTemplate.opsForSet().members(TODO_INDEX_KEY + event.getTodoId());
        if (notifyIds == null) {
            return ;
        }
        for (String notifyId : notifyIds) {
            redisTemplate.opsForZSet().remove(QUEUE_KEY, notifyId);
            redisTemplate.opsForSet().remove(USER_INDEX_KEY + event.getUserId(), notifyId);
            redisTemplate.delete("notify:" + notifyId);
        }
        redisTemplate.delete(TODO_INDEX_KEY + event.getTodoId());
    }

    public void deleteUserNotify(UserDeletedEvent event) {
        Set<String> notifyIds = redisTemplate.opsForSet().members(USER_INDEX_KEY + event.getUserId());
        if (notifyIds == null) {
            return ;
        }
        for (String notifyId : notifyIds) {
            redisTemplate.opsForZSet().remove(QUEUE_KEY, notifyId);
            redisTemplate.delete("notify:" + notifyId);
        }
        redisTemplate.delete(USER_INDEX_KEY + event.getUserId());
    }
}
