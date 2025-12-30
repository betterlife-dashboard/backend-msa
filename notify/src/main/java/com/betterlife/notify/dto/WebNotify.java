package com.betterlife.notify.dto;

import com.betterlife.notify.enums.NotifyType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class WebNotify {
    Long userId;
    NotifyType notifyType;
    String title;
    String body;
    LocalDateTime sendAt;
    Long todoId;
    String remainTime;
    String link;

    @Builder
    public WebNotify(Long userId, NotifyType notifyType, String title, String body, LocalDateTime sendAt, Long todoId, String remainTime, String link) {
        this.userId = userId;
        this.notifyType = notifyType;
        this.title = title;
        this.body = body;
        this.sendAt = sendAt;
        this.todoId = todoId;
        this.remainTime = remainTime;
        this.link = link;
    }
}
