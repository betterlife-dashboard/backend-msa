package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class TodoRequest {
    private String title;
    private TodoType type;
    private TodoStatus status = TodoStatus.PLANNED;
    private int repeatDays;
    private LocalDateTime activeFrom;
    private LocalDateTime activeUntil;

    @Builder
    public TodoRequest(String title, TodoType type, TodoStatus status, int repeatDays, LocalDateTime activeFrom, LocalDateTime activeUntil) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.repeatDays = repeatDays;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
    }
}
