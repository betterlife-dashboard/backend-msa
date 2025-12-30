package com.betterlife.notify.event;

import lombok.Data;

@Data
public class UserDeletedEvent {
    private Long userId;
}
