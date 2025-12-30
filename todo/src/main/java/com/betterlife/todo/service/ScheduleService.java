package com.betterlife.todo.service;

import com.betterlife.todo.client.UserClient;
import com.betterlife.todo.domain.Todo;
import com.betterlife.todo.dto.ScheduleRequest;
import com.betterlife.todo.dto.ScheduleUpdateRequest;
import com.betterlife.todo.dto.TodoResponse;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.event.ScheduleCreatedEvent;
import com.betterlife.todo.event.ScheduleDeletedEvent;
import com.betterlife.todo.exception.AccessDeniedException;
import com.betterlife.todo.message.EventProducer;
import com.betterlife.todo.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final TodoRepository todoRepository;
    private final EventProducer eventProducer;
    private final UserClient userClient;

    public TodoResponse createSchedule(Long userId, ScheduleRequest scheduleRequest) {
        userClient.getUser(userId);
        Todo todo = Todo.builder()
                .userId(userId)
                .title(scheduleRequest.getTitle())
                .type(scheduleRequest.getType())
                .status(TodoStatus.PLANNED)
                .repeatDays(0)
                .activeFrom(scheduleRequest.getActiveFrom())
                .activeUntil(scheduleRequest.getActiveUntil())
                .build();
        Todo saved = todoRepository.save(todo);
        scheduleRequest.getAlarms().forEach(alarm -> {
            String[] time = alarm.split("-");
            String deadlineTime = (time[0].equals("reminder")) ? saved.getActiveFrom().toString() : saved.getActiveUntil().toString();
            ScheduleCreatedEvent event = ScheduleCreatedEvent.builder()
                    .todoId(saved.getId())
                    .userId(saved.getUserId())
                    .title(saved.getTitle())
                    .standard(time[0])
                    .remainTime(time[1])
                    .deadlineTime(deadlineTime)
                    .build();
            eventProducer.sendScheduleCreatedEvent(event);
        });
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public TodoResponse updateSchedule(Long userId, Long todoId, ScheduleUpdateRequest request) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }

        todo.changeTitle(request.getTitle());
        todo.changeType(request.getType());
        todo.updateStatus(request.getStatus());
        todo.changeActiveDate(request.getActiveFrom(), request.getActiveUntil());
        eventProducer.sendScheduleDeletedEvent(ScheduleDeletedEvent.builder()
                .todoId(todo.getId())
                .userId(todo.getUserId())
                .build());
        request.getAlarms().forEach(alarm -> {
            String[] time = alarm.split("-");
            String deadlineTime = (time[0].equals("reminder")) ? todo.getActiveFrom().toString() : todo.getActiveUntil().toString();
            ScheduleCreatedEvent event = ScheduleCreatedEvent.builder()
                    .todoId(todo.getId())
                    .userId(todo.getUserId())
                    .title(todo.getTitle())
                    .standard(time[0])
                    .remainTime(time[1])
                    .deadlineTime(deadlineTime)
                    .build();
            eventProducer.sendScheduleCreatedEvent(event);
        });
        return TodoResponse.fromEntity(todo);
    }
}
