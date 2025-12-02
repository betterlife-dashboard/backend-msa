package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ScheduleRequest {
    private String title;
    private TodoType type;
    private TodoStatus status = TodoStatus.PLANNED;
    private LocalDateTime activeFrom;
    private LocalDateTime activeUntil;
    private List<String> alarms;

    @Builder
    public ScheduleRequest(
            String title,
            TodoType type,
            TodoStatus status,
            LocalDateTime activeFrom,
            LocalDateTime activeUntil,
            List<String> alarms
    ) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
        this.alarms = alarms;
    }
}
