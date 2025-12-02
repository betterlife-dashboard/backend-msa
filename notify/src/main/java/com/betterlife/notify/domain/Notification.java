package com.betterlife.notify.domain;

import com.betterlife.notify.enums.EventType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Table(name = "notifications")
@Entity
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long todoId;

    @Column(columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    private String remainTime;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(name = "send_at", nullable = false)
    private LocalDateTime sendAt;

    @Column(nullable = false)
    private Boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "read_at")
    private Timestamp readAt;

    public void read() {
        this.isRead = true;
        this.readAt = Timestamp.valueOf(LocalDateTime.now());
    }

    @Builder
    public Notification(
            Long userId,
            Long todoId,
            EventType eventType,
            String remainTime,
            String title,
            String body,
            LocalDateTime sendAt
    ) {
        this.userId = userId;
        this.todoId = todoId;
        this.eventType = eventType;
        this.remainTime = remainTime;
        this.title = title;
        this.body = body;
        this.sendAt = sendAt;
        this.isRead = false;
    }
}
