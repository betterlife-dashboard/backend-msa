package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class TodoRequest {
    private String title;
    private TodoType type;
    private TodoStatus status = TodoStatus.PLANNED;
    private Integer repeatDays;
    private LocalDateTime activeFrom;
    private LocalDateTime activeUntil;

    @Builder
    public TodoRequest(String title,
                       TodoType type,
                       TodoStatus status,
                       Integer repeatDays,
                       LocalDateTime activeFrom,
                       LocalDateTime activeUntil) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.repeatDays = repeatDays;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
    }
}
