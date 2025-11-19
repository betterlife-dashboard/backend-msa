package com.betterlife.todo.service;

import com.betterlife.todo.domain.Todo;
import com.betterlife.todo.dto.*;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.exception.AccessDeniedException;
import com.betterlife.todo.exception.InvalidRequestException;
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

    public List<TodoResponse> getTodosByDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return todoRepository.findAllByUserIdAndDateWithinActivePeriod(userId, start, end)
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
                    .activeFrom(LocalDateTime.now())
                    .activeUntil(LocalDateTime.now())
                    .build();
            todo.addChildTodo(child);
        }
        Todo saved = todoRepository.save(todo);
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
        todoRepository.deleteById(todoId);
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
                        .activeFrom(LocalDateTime.now())
                        .activeUntil(LocalDateTime.now())
                        .build();
                todo.addChildTodo(child);
            }
        }
    }

    @Transactional
    public void closePastTodos() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Todo> todos = todoRepository.findAllByTodoStatusPlannedAndActiveUntilBefore(currentTime);
        for (Todo todo : todos) {
            todo.updateStatus(TodoStatus.EXPIRED);
        }
    }
}
