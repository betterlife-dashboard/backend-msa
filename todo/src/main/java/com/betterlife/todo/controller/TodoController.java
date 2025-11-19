package com.betterlife.todo.controller;

import com.betterlife.todo.dto.RepeatTodoUpdateRequest;
import com.betterlife.todo.dto.TodoRequest;
import com.betterlife.todo.dto.TodoResponse;
import com.betterlife.todo.dto.TodoUpdateRequest;
import com.betterlife.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping("/{date}")
    public ResponseEntity<List<TodoResponse>> getTodosByDate(@PathVariable LocalDate date, @RequestHeader("X-User-Id") Long userId) {
        List<TodoResponse> todos = todoService.getTodosByDate(userId, date);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<TodoResponse> getTodoDetail(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse todoResponse = todoService.getTodoById(userId, id);
        return ResponseEntity.ok(todoResponse);
    }

    @GetMapping("/recur")
    public ResponseEntity<List<TodoResponse>> getRecurTodos(@RequestHeader("X-User-Id") Long userId) {
        List<TodoResponse> todos = todoService.getRecurTodos(userId);
        return ResponseEntity.ok(todos);
    }

    @PatchMapping("/patch/{id}")
    public ResponseEntity<TodoResponse> updateTodo(@PathVariable Long id, @RequestBody TodoUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse updated = todoService.updateTodo(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/patch/repeat/{id}")
    public ResponseEntity<TodoResponse> updateRepeatTodo(@PathVariable Long id, @RequestBody RepeatTodoUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse updated = todoService.updateRepeatTodo(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/create")
    public ResponseEntity<TodoResponse> createTodo(@RequestBody TodoRequest todoRequest, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse todoResponse = todoService.createTodo(userId, todoRequest);
        return ResponseEntity.ok(todoResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        todoService.deleteTodo(userId, id);
        return ResponseEntity.noContent().build();
    }

}
