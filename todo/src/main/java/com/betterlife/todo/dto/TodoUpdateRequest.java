package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
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

}
