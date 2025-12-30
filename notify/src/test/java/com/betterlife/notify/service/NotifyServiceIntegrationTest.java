package com.betterlife.notify.service;

import com.betterlife.notify.event.ScheduleCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class NotifyServiceIntegrationTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        try (RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()) {
            connection.serverCommands();
        }
    }

    @Test
    void createScheduleNotify_persistsToRedis() {
        ScheduleCreatedEvent event = ScheduleCreatedEvent.builder()
                .todoId(9L)
                .userId(3L)
                .title("Report")
                .standard("deadline")
                .remainTime("1h")
                .deadlineTime("2025-01-01T12:00:00")
                .build();

        notifyService.createScheduleNotify(event);

        assertThat(redisTemplate.opsForSet().members("notify:index:todo:9")).isNotEmpty();
        assertThat(redisTemplate.opsForSet().members("notify:index:user:3")).isNotEmpty();
        assertThat(redisTemplate.opsForZSet().range("notify:schedule", 0, -1)).isNotEmpty();
    }
}
