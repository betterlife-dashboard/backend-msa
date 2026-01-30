package com.betterlife.todo.dto;

import com.betterlife.todo.domain.TodoEntity;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TodoResponse {

    private Long id;
    private Long recurTaskId;
    private TodoType todoType;
    private TodoStatus todoStatus;
    private String title;
    private String memo;
    private boolean allDay;
    private LocalDate occurrenceDate;

    @Schema(type = "string", format = "time", example = "00:00:00", nullable = true)
    private LocalTime atTime;
    private LocalDateTime completedAt;
    private Integer durationSec;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TodoResponse fromEntity(TodoEntity todo) {
        TodoResponse dto = new TodoResponse();
        dto.id = todo.getId();
        dto.recurTaskId = todo.getRecurTaskId();
        dto.todoType = todo.getTodoType();
        dto.todoStatus = todo.getTodoStatus();
        dto.title = todo.getTitle();
        dto.memo = todo.getMemo();
        dto.allDay = todo.isAllDay();
        dto.occurrenceDate = todo.getOccurrenceDate();
        dto.atTime = todo.getAtTime();
        dto.completedAt = todo.getCompletedAt();
        dto.durationSec = todo.getDurationSec();
        dto.createdAt = todo.getCreatedAt();
        dto.updatedAt = todo.getUpdatedAt();
        return dto;
    }
}
