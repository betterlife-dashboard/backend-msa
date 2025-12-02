package com.betterlife.notify.service;

import com.betterlife.notify.domain.Notification;
import com.betterlife.notify.event.TodoEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Sql(scripts = "/V2__insert_template_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class NotifyServiceTest {

    @Autowired
    private NotifyService notifyService;

    @Test
    public void createDeadlineNotification() {
        TodoEvent event = TodoEvent.builder()
                .eventType("create")
                .todoId(1L)
                .userId(1L)
                .title("test")
                .remainTime("1d")
                .deadline(LocalDateTime.now().toString())
                .build();
        notifyService.createDeadlineNotification(event);
        List<Notification> list = notifyService.getNotificationById(1L);
        assertThat(list.get(0).getTitle()).isEqualTo("test 마감이 임박했어요");
    }

}