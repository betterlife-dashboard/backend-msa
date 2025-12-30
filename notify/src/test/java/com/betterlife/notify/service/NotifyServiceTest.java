package com.betterlife.notify.service;

import com.betterlife.notify.dto.WebNotify;
import com.betterlife.notify.enums.NotifyType;
import com.betterlife.notify.event.ScheduleCreatedEvent;
import com.betterlife.notify.event.ScheduleDeletedEvent;
import com.betterlife.notify.event.UserDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private NotifyService notifyService;

    @Test
    void createScheduleNotify_storesPayloadAndIndexes() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        AtomicReference<WebNotify> captured = new AtomicReference<>();
        when(objectMapper.writeValueAsString(any(WebNotify.class))).thenAnswer(invocation -> {
            captured.set(invocation.getArgument(0));
            return "json";
        });

        ScheduleCreatedEvent event = ScheduleCreatedEvent.builder()
                .todoId(9L)
                .userId(3L)
                .title("Report")
                .standard("deadline")
                .remainTime("1h")
                .deadlineTime("2025-01-01T12:00:00")
                .build();

        notifyService.createScheduleNotify(event);

        WebNotify notify = captured.get();
        assertThat(notify).isNotNull();
        assertThat(notify.getUserId()).isEqualTo(3L);
        assertThat(notify.getTodoId()).isEqualTo(9L);
        assertThat(notify.getNotifyType()).isEqualTo(NotifyType.DEADLINE);
        assertThat(notify.getTitle()).isEqualTo("데드라인: Report");
        assertThat(notify.getBody()).isEqualTo("Report 마감 1시간 전입니다.");
        assertThat(notify.getSendAt()).isEqualTo(LocalDateTime.parse("2025-01-01T12:00:00").minusHours(1));
        assertThat(notify.getRemainTime()).isEqualTo("1h");
        assertThat(notify.getLink()).isEqualTo("https://www.betterlife.betterlifeboard.com");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), eq("json"));
        String notifyKey = keyCaptor.getValue();
        assertThat(notifyKey).startsWith("notify:");
        String notifyId = notifyKey.substring("notify:".length());

        long expectedScore = notify.getSendAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        verify(zSetOperations).add(eq("notify:schedule"), eq(notifyId), eq((double) expectedScore));
        verify(setOperations).add(eq("notify:index:todo:9"), eq(notifyId));
        verify(setOperations).add(eq("notify:index:user:3"), eq(notifyId));
    }

    @Test
    void getNotifies_returnsNotifiesFromRedis() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(setOperations.members("notify:index:todo:9")).thenReturn(Set.of("id-1", "id-2"));

        WebNotify first = WebNotify.builder()
                .userId(1L)
                .notifyType(NotifyType.REMINDER)
                .title("t1")
                .body("b1")
                .sendAt(LocalDateTime.parse("2025-01-01T10:00:00"))
                .todoId(9L)
                .remainTime("1h")
                .link("link-1")
                .build();
        WebNotify second = WebNotify.builder()
                .userId(2L)
                .notifyType(NotifyType.DEADLINE)
                .title("t2")
                .body("b2")
                .sendAt(LocalDateTime.parse("2025-01-02T10:00:00"))
                .todoId(9L)
                .remainTime("1d")
                .link("link-2")
                .build();

        when(valueOperations.get("notify:id-1")).thenReturn("json-1");
        when(valueOperations.get("notify:id-2")).thenReturn("json-2");
        when(objectMapper.readValue("json-1", WebNotify.class)).thenReturn(first);
        when(objectMapper.readValue("json-2", WebNotify.class)).thenReturn(second);

        List<WebNotify> result = notifyService.getNotifies(9L);

        assertThat(result).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void deleteScheduleNotify_removesRelatedEntries() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(setOperations.members("notify:index:todo:9")).thenReturn(Set.of("id-1", "id-2"));

        ScheduleDeletedEvent event = new ScheduleDeletedEvent();
        event.setTodoId(9L);
        event.setUserId(3L);

        notifyService.deleteScheduleNotify(event);

        verify(zSetOperations).remove("notify:schedule", "id-1");
        verify(zSetOperations).remove("notify:schedule", "id-2");
        verify(setOperations).remove("notify:index:user:3", "id-1");
        verify(setOperations).remove("notify:index:user:3", "id-2");
        verify(redisTemplate).delete("notify:id-1");
        verify(redisTemplate).delete("notify:id-2");
        verify(redisTemplate).delete("notify:index:todo:9");
    }

    @Test
    void deleteUserNotify_removesRelatedEntries() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(setOperations.members("notify:index:user:3")).thenReturn(Set.of("id-1"));

        UserDeletedEvent event = new UserDeletedEvent();
        event.setUserId(3L);

        notifyService.deleteUserNotify(event);

        verify(zSetOperations).remove("notify:schedule", "id-1");
        verify(redisTemplate).delete("notify:id-1");
        verify(redisTemplate).delete("notify:index:user:3");
    }
}
