package com.betterlife.auth.event;

import lombok.Builder;
import lombok.Data;

@Data
public class UserDeletedEvent {
    private Long id;

    @Builder
    public UserDeletedEvent(Long id) {
        this.id = id;
    }
}
