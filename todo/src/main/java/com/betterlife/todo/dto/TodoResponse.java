package com.betterlife.todo.dto;

import com.betterlife.todo.domain.Todo;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TodoResponse {

    private Long id;
    private String title;
    private TodoType type = TodoType.GENERAL;
    private TodoStatus status = TodoStatus.PLANNED;
    private Integer repeatDays;
    private LocalDateTime activeFrom;
    private LocalDateTime activeUntil;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public static TodoResponse fromEntity(Todo todo) {
        TodoResponse dto = new TodoResponse();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setType(todo.getType());
        dto.setStatus(todo.getStatus());
        dto.setRepeatDays(todo.getRepeatDays());
        dto.setActiveFrom(todo.getActiveFrom());
        dto.setActiveUntil(todo.getActiveUntil());
        dto.setCreatedAt(todo.getCreatedAt());
        dto.setUpdatedAt(todo.getUpdatedAt());
        return dto;
    }
}
