package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class TodoUpdateRequest {
    private String title;
    private TodoType type;
    private TodoStatus status = TodoStatus.PLANNED;
    private LocalDateTime activeFrom;
    private LocalDateTime activeUntil;

    @Builder
    public TodoUpdateRequest(String title,
                             TodoType type,
                             TodoStatus status,
                             LocalDateTime activeFrom,
                             LocalDateTime activeUntil) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
    }
}
