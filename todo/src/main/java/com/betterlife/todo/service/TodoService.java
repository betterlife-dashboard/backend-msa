package com.betterlife.todo.service;

import com.betterlife.todo.domain.TodoEntity;
import com.betterlife.todo.dto.*;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import com.betterlife.todo.event.EventProducer;
import com.betterlife.todo.exception.AccessDeniedException;
import com.betterlife.todo.exception.TodoNotFoundException;
import com.betterlife.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final EventProducer eventProducer;

    public TodoResponse getTodoById(Long userId, Long todoId) {
        TodoEntity todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }
        return TodoResponse.fromEntity(todo);
    }

    public TodoResponse createTodo(Long userId, TodoCreateRequest todoCreateRequest) {
        TodoEntity todo = TodoEntity.builder()
                .userId(userId)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title(todoCreateRequest.getTitle())
                .memo(todoCreateRequest.getMemo())
                .allDay(todoCreateRequest.isAllDay())
                .occurrenceDate(todoCreateRequest.getOccurrenceDate())
                .atTime(todoCreateRequest.getAtTime())
                .completedAt(null)
                .durationSec(null)
                .build();
        TodoEntity saved = todoRepository.save(todo);
        return TodoResponse.fromEntity(saved);
    }

    public void deleteTodo(Long userId, Long todoId) {
        TodoEntity todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }
        eventProducer.sendTodoDeletedEvent(todoId);
        todoRepository.deleteById(todoId);
    }

    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoUpdateRequest todoRequest) {
        TodoEntity todo = todoRepository.findById(todoId).orElseThrow(() -> new TodoNotFoundException("존재하지 않는 Todo입니다."));
        if (!todo.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
        }
        todo.update(todoRequest);
        eventProducer.sendTodoUpdatedEvent(todoId, todoRequest.getReminderMask());
        return TodoResponse.fromEntity(todo);
    }

    @Transactional
    public void deleteUser(Long userId) {
        todoRepository.deleteAllByUserId(userId);
    }

//    public List<TodoResponse> getTodosByDate(Long userId, LocalDate date) {
//        LocalDateTime todayStart = date.atStartOfDay();
//        LocalDateTime todayEnd = date.plusDays(1).atStartOfDay().minusSeconds(1);
//        return todoRepository.findAllByUserIdAndActiveFromBeforeAndActiveUntilAfter(userId, todayEnd, todayStart)
//                .stream()
//                .map(TodoResponse::fromEntity)
//                .toList();
//    }
//
//    public List<TodoResponse> getTodosByScheduledAndMonth(Long userId, LocalDate month) {
//        LocalDateTime monthStart = month.atStartOfDay();
//        LocalDateTime monthEnd = month.plusMonths(1).atStartOfDay().minusSeconds(1);
//        return todoRepository.findAllByUserIdAndTypeAndActiveFromBeforeAndActiveUntilAfter(userId, TodoType.SCHEDULE, monthEnd, monthStart)
//                .stream()
//                .map(TodoResponse::fromEntity)
//                .toList();
//    }

//    @Transactional
//    public TodoResponse updateRepeatTodo(Long userId, Long todoId, RepeatTodoUpdateRequest todoRequest) {
//        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Todo입니다."));
//        if (!todo.getUserId().equals(userId)) {
//            throw new AccessDeniedException("이 Todo에 접근할 권한이 없습니다.");
//        } else if (todoRequest.getRepeatDays() == 0) {
//            throw new InvalidRequestException("반복용 Todo는 반드시 반복되어야 합니다.");
//        }
//
//        todo.changeTitle(todoRequest.getTitle());
//        todo.changeType(todoRequest.getType());
//        todo.changeRepeatDays(todoRequest.getRepeatDays());
//
//        return TodoResponse.fromEntity(todo);
//    }

//    public List<TodoResponse> getRecurTodos(Long userId) {
//        return todoRepository.findAllByUserIdAndIsRecurring(userId, true)
//                .stream()
//                .map(TodoResponse::fromEntity)
//                .toList();
//    }
//
//    public Boolean checkRepeatDate(Integer weeks) {
//        LocalDate today = LocalDate.now();
//        DayOfWeek dayOfWeek = today.getDayOfWeek();
//        int now = 1 << (dayOfWeek.getValue() - 1);
//        return (weeks & now) != 0;
//    }
//
//    @Transactional
//    public void generateRecurringTodos() {
//        List<Todo> todos = todoRepository.findAllByIsRecurring(true);
//        for (Todo todo : todos) {
//            if (checkRepeatDate(todo.getRepeatDays())) {
//                Todo child = Todo.builder()
//                        .userId(todo.getUserId())
//                        .title(todo.getTitle())
//                        .type(todo.getType())
//                        .status(TodoStatus.PLANNED)
//                        .repeatDays(0)
//                        .activeFrom(LocalDate.now().atStartOfDay())
//                        .activeUntil(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
//                        .build();
//                todo.addChildTodo(child);
//            }
//        }
//    }
//
//    @Transactional
//    public void closePastTodos() {
//        LocalDateTime currentTime = LocalDateTime.now();
//        List<Todo> todos = todoRepository.findAllByStatusAndActiveUntilBefore(TodoStatus.PLANNED, currentTime);
//        todos.forEach(todo -> {
//                todo.updateStatus(TodoStatus.EXPIRED);
//        });
//    }
}
