package com.betterlife.todo.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleDeletedEvent {
    private Long todoId;
    private Long userId;
}
