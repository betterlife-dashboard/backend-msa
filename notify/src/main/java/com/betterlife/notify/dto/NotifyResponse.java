package com.betterlife.notify.dto;

import com.betterlife.notify.domain.Notification;
import com.betterlife.notify.enums.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotifyResponse {
    private Long id;
    private Long todoId;
    private EventType eventType;
    private String title;
    private String body;

    public static NotifyResponse fromEntity(Notification notification) {
        NotifyResponse notifyResponse = new NotifyResponse();
        notifyResponse.id = notification.getId();
        notifyResponse.todoId = notification.getTodoId();
        notifyResponse.eventType = notification.getEventType();
        notifyResponse.title = notification.getTitle();
        notifyResponse.body= notification.getBody();

        return notifyResponse;
    }
}
