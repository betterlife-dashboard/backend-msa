package com.betterlife.todo.event;

import lombok.Data;

@Data
public class UserDeletedEvent {
    private Long userId;
}
