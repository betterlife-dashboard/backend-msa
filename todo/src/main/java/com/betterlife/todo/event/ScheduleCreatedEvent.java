package com.betterlife.todo.event;

import lombok.Builder;
import lombok.Data;

@Data
public class ScheduleCreatedEvent {
    private Long todoId;
    private Long userId;
    private String title;
    private String standard;
    private String remainTime;
    private String deadlineTime;

    @Builder
    public ScheduleCreatedEvent(
            Long todoId,
            Long userId,
            String title,
            String standard,
            String remainTime,
            String deadlineTime
    ) {
        this.todoId = todoId;
        this.userId = userId;
        this.title = title;
        this.standard = standard;
        this.remainTime = remainTime;
        this.deadlineTime = deadlineTime;
    }
}
