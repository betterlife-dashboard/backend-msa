package com.betterlife.todo.event;

import com.betterlife.todo.domain.Todo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoEvent {
    private String eventType;
    private Long todoId;
    private Long userId;
    private String title;
    private String remainTime;
    private String deadline;

    public static TodoEvent fromEntity(Todo todo, String eventType, String stand, String deadline) {
        TodoEvent deadlineEvent = new TodoEvent();
        deadlineEvent.eventType = eventType;
        deadlineEvent.todoId = todo.getId();
        deadlineEvent.userId = todo.getUserId();
        deadlineEvent.title = todo.getTitle();
        deadlineEvent.remainTime = deadline;
        LocalDateTime time;
        if (stand.equals("reminder")) {
            time = todo.getActiveFrom();
        } else {
            time = todo.getActiveUntil();
        }
        if (eventType.equals("create")) {
            if (deadline.equals("1h")) {
                time = time.plusHours(1);
            } else if (deadline.equals("1d")) {
                time = time.plusDays(1);
            } else if (deadline.equals("3d")) {
                time = time.plusDays(3);
            } else if (deadline.equals("1w")) {
                time = time.plusWeeks(1);
            }
        }
        deadlineEvent.deadline = time.toString();
        return deadlineEvent;
    }
}
