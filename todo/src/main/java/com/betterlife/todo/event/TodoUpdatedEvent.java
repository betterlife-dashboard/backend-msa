package com.betterlife.todo.event;

import lombok.Data;

@Data
public class TodoUpdatedEvent {
    private Long id;
    private Byte reminderMask;

    public TodoUpdatedEvent(Long id, Byte reminderMask) {
        this.id = id;
        this.reminderMask = reminderMask;
    }
}
