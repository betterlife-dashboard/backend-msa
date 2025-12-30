package com.betterlife.notify.event;

import lombok.Data;

@Data
public class ScheduleDeletedEvent {
    private Long todoId;
    private Long userId;
}
