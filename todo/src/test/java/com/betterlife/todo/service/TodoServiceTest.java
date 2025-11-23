package com.betterlife.todo.service;

import com.betterlife.todo.dto.TodoRequest;
import com.betterlife.todo.dto.TodoResponse;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Test
    void getTodosByScheduledAndMonth() {
        for (int i = 0; i < 10; i++) {
            TodoRequest test = TodoRequest.builder()
                    .title("test" + i)
                    .type(TodoType.SCHEDULE)
                    .status(TodoStatus.PLANNED)
                    .repeatDays(0)
                    .activeFrom(LocalDate.now().minusDays(i).atStartOfDay())
                    .activeUntil(LocalDate.now().minusDays(i).atStartOfDay())
                    .build();
            todoService.createTodo(1L, test);
        }
        List<TodoResponse> todos = todoService.getTodosByScheduledAndMonth(1L, LocalDate.now().minusDays(22));
        todos.forEach(todo -> System.out.println("todo = " + todo.getTitle()));
    }
}