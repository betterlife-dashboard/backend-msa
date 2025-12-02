package com.betterlife.notify.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TodoEvent {
    private String eventType;
    private Long todoId;
    private Long userId;
    private String title;
    private String remainTime;
    private String deadline;

    @Builder
    public TodoEvent(String eventType, Long todoId, Long userId, String title, String remainTime, String deadline) {
        this.eventType = eventType;
        this.todoId = todoId;
        this.userId = userId;
        this.title = title;
        this.remainTime = remainTime;
        this.deadline = deadline;
    }
}
