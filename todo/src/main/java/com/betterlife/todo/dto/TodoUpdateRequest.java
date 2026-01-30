package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class TodoUpdateRequest {
    private TodoStatus todoStatus = TodoStatus.PENDING;
    private String title;
    private String memo;
    private boolean allDay;
    private LocalDate occurrenceDate;
    private LocalTime atTime;
    private Byte reminderMask;
}
