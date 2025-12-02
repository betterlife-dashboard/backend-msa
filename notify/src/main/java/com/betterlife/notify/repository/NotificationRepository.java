package com.betterlife.notify.repository;

import com.betterlife.notify.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByTodoId(Long todoId);

    void deleteNotificationByTodoId(Long todoId);

    void deleteNotificationByUserId(Long todoId);

    List<Notification> findAllByUserIdAndTodoId(Long userId, Long todoId);

    List<Notification> findAllByUserId(Long userId);

    List<Notification> findAllByUserIdAndSendAtBeforeAndIsRead(Long userId, LocalDateTime sendAtBefore, Boolean isRead);
}
