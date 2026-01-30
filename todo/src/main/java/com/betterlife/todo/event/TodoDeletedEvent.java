package com.betterlife.todo.event;

import lombok.Builder;
import lombok.Data;

@Data
public class TodoDeletedEvent {
    private Long id;

    @Builder
    public TodoDeletedEvent(Long id) {
        this.id = id;
    }
}
