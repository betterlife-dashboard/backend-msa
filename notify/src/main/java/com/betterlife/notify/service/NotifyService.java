package com.betterlife.notify.service;

import com.betterlife.notify.domain.Notification;
import com.betterlife.notify.domain.NotificationTemplate;
import com.betterlife.notify.dto.NotifyResponse;
import com.betterlife.notify.dto.TodoNotifyDetailResponse;
import com.betterlife.notify.enums.ChannelType;
import com.betterlife.notify.enums.EventType;
import com.betterlife.notify.enums.Lang;
import com.betterlife.notify.event.TodoEvent;
import com.betterlife.notify.repository.NotificationChannelRepository;
import com.betterlife.notify.repository.NotificationRepository;
import com.betterlife.notify.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyService {

    private final NotificationRepository notificationRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm");

    public List<NotifyResponse> getNotificationsNow(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByUserId(userId);
        return new ArrayList<>();
    }

    public TodoNotifyDetailResponse getNotificationByTodoId(Long userId, Long todoId) {
        List<Notification> notifications = notificationRepository.findAllByUserIdAndTodoId(userId, todoId);
        TodoNotifyDetailResponse todoNotifyDetailResponse = new TodoNotifyDetailResponse();
        notifications.forEach(notification -> {
            todoNotifyDetailResponse.addAlarm(notification.getEventType(), notification.getRemainTime());
        });
        return todoNotifyDetailResponse;
    }

    public List<Notification> getNotificationById(Long todoId) {
        return notificationRepository.findAllByTodoId(todoId);
    }

    public void createDeadlineNotification(TodoEvent event) {
        NotificationTemplate nt = notificationTemplateRepository.findByEventTypeAndChannelTypeAndLang(EventType.SCHEDULE_DEADLINE, ChannelType.WEB, Lang.KO)
                        .orElseThrow(() -> new RuntimeException("해당 템플릿을 찾을 수 없습니다."));
        LocalDateTime deadline = LocalDateTime.parse(event.getDeadline());
        String title = render(
                nt.getTitleTemplate(),
                Map.of("title", event.getTitle())
        );
        String remainTime = "";
        if (event.getRemainTime().equals("1h")) remainTime = "1시간";
        else if (event.getRemainTime().equals("1d")) remainTime = "1일";
        else if (event.getRemainTime().equals("3d")) remainTime = "3일";
        else if (event.getRemainTime().equals("1w")) remainTime = "1주";
        String body = render(
                nt.getBodyTemplate(),
                Map.of(
                        "title", event.getTitle(),
                        "deadline", deadline.format(formatter),
                        "timeLeft", remainTime
                )
        );
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .todoId(event.getTodoId())
                .eventType(EventType.SCHEDULE_DEADLINE)
                .remainTime(event.getRemainTime())
                .title(title)
                .body(body)
                .sendAt(deadline)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteTodoNotification(TodoEvent event) {
        notificationRepository.deleteNotificationByTodoId(event.getTodoId());
    }

    @Transactional
    public void deleteUserNotification(TodoEvent event) {
        notificationRepository.deleteNotificationByUserId(event.getTodoId());
    }

    public void createReminderNotification(TodoEvent event) {
        NotificationTemplate nt = notificationTemplateRepository.findByEventTypeAndChannelTypeAndLang(EventType.SCHEDULE_REMINDER, ChannelType.WEB, Lang.KO)
                .orElseThrow(() -> new RuntimeException("해당 템플릿을 찾을 수 없습니다."));
        LocalDateTime deadline = LocalDateTime.parse(event.getDeadline());
        String title = render(
                nt.getTitleTemplate(),
                Map.of("title", event.getTitle())
        );
        String remainTime = "";
        if (event.getRemainTime().equals("1h")) remainTime = "1시간";
        else if (event.getRemainTime().equals("1d")) remainTime = "1일";
        else if (event.getRemainTime().equals("3d")) remainTime = "3일";
        else if (event.getRemainTime().equals("1w")) remainTime = "1주";
        String body = render(
                nt.getBodyTemplate(),
                Map.of(
                        "title", event.getTitle(),
                        "deadline", deadline.format(formatter),
                        "timeLeft", remainTime
                )
        );
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .todoId(event.getTodoId())
                .eventType(EventType.SCHEDULE_REMINDER)
                .remainTime(event.getRemainTime())
                .title(title)
                .body(body)
                .sendAt(deadline)
                .build();
        notificationRepository.save(notification);
    }

    private String render(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }
}
