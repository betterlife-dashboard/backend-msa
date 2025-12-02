package com.betterlife.todo.service;

import com.betterlife.todo.client.UserClient;
import com.betterlife.todo.domain.Todo;
import com.betterlife.todo.dto.*;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import com.betterlife.todo.event.TodoEvent;
import com.betterlife.todo.exception.AccessDeniedException;
import com.betterlife.todo.exception.InvalidRequestException;
import com.betterlife.todo.producer.EventProducer;
import com.betterlife.todo.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserClient userClient;
    private final EventProducer eventProducer;

    public List<TodoResponse> getTodosByDate(Long userId, LocalDate date) {
        LocalDateTime todayStart = date.atStartOfDay();
        LocalDateTime todayEnd = date.plusDays(1).atStartOfDay().minusSeconds(1);
        return todoRepository.findAllByUserIdAndActiveFromBeforeAndActiveUntilAfter(userId, todayEnd, todayStart)
                .stream()
                .map(TodoResponse::fromEntity)
                .toList();
    }

    public List<TodoResponse> getTodosByScheduledAndMonth(Long userId, LocalDate month) {
        LocalDateTime monthStart = month.atStartOfDay();
        LocalDateTime monthEnd = month.plusMonths(1).atStartOfDay().minusSeconds(1);
        return todoRepository.findAllByUserIdAndTypeAndActiveFromBeforeAndActiveUntilAfter(userId, TodoType.SCHEDULE, monthEnd, monthStart)
                .stream()
                .map(TodoResponse::fromEntity)
                .toList();
    }

    public TodoResponse getTodoById(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }
        return TodoResponse.fromEntity(todo);
    }

    public TodoResponse createTodo(Long userId, TodoRequest todoRequest) {
        userClient.getUser(userId);
        LocalDateTime activeFrom = todoRequest.getActiveFrom();
        LocalDateTime activeUntil = todoRequest.getActiveUntil();
        if (todoRequest.getRepeatDays() == 0 && activeFrom.isAfter(activeUntil)) {
            throw new InvalidRequestException("날짜 설정이 잘못되었습니다.");
        }
        if (todoRequest.getRepeatDays() != 0) {
            activeFrom = null;
            activeUntil = null;
        }
        Todo todo = Todo.builder()
                .userId(userId)
                .title(todoRequest.getTitle())
                .type(todoRequest.getType())
                .status(TodoStatus.PLANNED)
                .repeatDays(todoRequest.getRepeatDays())
                .activeFrom(activeFrom)
                .activeUntil(activeUntil)
                .build();
        if (checkRepeatDate(todoRequest.getRepeatDays())) {
            Todo child = Todo.builder()
                    .userId(userId)
                    .title(todoRequest.getTitle())
                    .type(todoRequest.getType())
                    .status(TodoStatus.PLANNED)
                    .repeatDays(0)
                    .activeFrom(LocalDate.now().atStartOfDay())
                    .activeUntil(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                    .build();
            todo.addChildTodo(child);
        }
        Todo saved = todoRepository.save(todo);
        return TodoResponse.fromEntity(saved);
    }

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
            TodoEvent event = TodoEvent.fromEntity(saved, "create", time[0], time[1]);
            if (time[0].equals("deadline")) {
                eventProducer.sendDeadline(event);
            } else {
                eventProducer.sendReminder(event);
            }
        });
        return TodoResponse.fromEntity(saved);
    }

    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoUpdateRequest todoRequest) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }

        todo.changeTitle(todoRequest.getTitle());
        todo.changeType(todoRequest.getType());
        todo.updateStatus(todoRequest.getStatus());
        todo.changeActiveDate(todoRequest.getActiveFrom(), todoRequest.getActiveUntil());

        return TodoResponse.fromEntity(todo);
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
        eventProducer.sendDeadline(TodoEvent.fromEntity(todo, "delete-todo", "", ""));
        eventProducer.sendReminder(TodoEvent.fromEntity(todo, "delete-todo", "", ""));
        request.getAlarms().forEach(alarm -> {
            String[] time = alarm.split("-");
            if (time[0].equals("deadline")) {
                TodoEvent event = TodoEvent.fromEntity(todo, "create", time[0], time[1]);
                eventProducer.sendDeadline(event);
            } else {
                TodoEvent event = TodoEvent.fromEntity(todo, "create", time[0], time[1]);
                eventProducer.sendReminder(event);
            }
        });
        return TodoResponse.fromEntity(todo);
    }

    @Transactional
    public TodoResponse updateRepeatTodo(Long userId, Long todoId, RepeatTodoUpdateRequest todoRequest) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        } else if (todoRequest.getRepeatDays() == 0) {
            throw new InvalidRequestException("반복용 Todo는 반드시 반복되어야 합니다.");
        }

        todo.changeTitle(todoRequest.getTitle());
        todo.changeType(todoRequest.getType());
        todo.changeRepeatDays(todoRequest.getRepeatDays());

        return TodoResponse.fromEntity(todo);
    }

    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }
        eventProducer.sendDeadline(TodoEvent.fromEntity(todo, "delete-todo",  "", ""));
        todoRepository.deleteById(todoId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        List<Todo> allByUserId = todoRepository.findAllByUserId(userId);
        if (allByUserId.isEmpty()) {
            return ;
        }
        eventProducer.sendDeadline(TodoEvent.fromEntity(allByUserId.get(0), "delete-user", "", ""));
        todoRepository.deleteAllByUserId(userId);
    }

    public List<TodoResponse> getRecurTodos(Long userId) {
        return todoRepository.findAllByUserIdAndIsRecurring(userId, true)
                .stream()
                .map(TodoResponse::fromEntity)
                .toList();
    }

    public Boolean checkRepeatDate(Integer weeks) {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        int now = 1 << (dayOfWeek.getValue() - 1);
        return (weeks & now) != 0;
    }

    @Transactional
    public void generateRecurringTodos() {
        List<Todo> todos = todoRepository.findAllByIsRecurring(true);
        for (Todo todo : todos) {
            if (checkRepeatDate(todo.getRepeatDays())) {
                Todo child = Todo.builder()
                        .userId(todo.getUserId())
                        .title(todo.getTitle())
                        .type(todo.getType())
                        .status(TodoStatus.PLANNED)
                        .repeatDays(0)
                        .activeFrom(LocalDate.now().atStartOfDay())
                        .activeUntil(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                        .build();
                todo.addChildTodo(child);
            }
        }
    }

    @Transactional
    public void closePastTodos() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Todo> todos = todoRepository.findAllByStatusAndActiveUntilBefore(TodoStatus.PLANNED, currentTime);
        todos.forEach(todo -> todo.updateStatus(TodoStatus.EXPIRED));
    }
}
