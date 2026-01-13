package com.betterlife.todo.controller;

import com.betterlife.todo.dto.*;
import com.betterlife.todo.service.ScheduleService;
import com.betterlife.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.links.LinkParameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
//    private final ScheduleService scheduleService;

//    @GetMapping("/{date}")
//    public ResponseEntity<List<TodoResponse>> getTodosByDate(@PathVariable("date") LocalDate date, @RequestHeader("X-User-Id") Long userId) {
//        List<TodoResponse> todos = todoService.getTodosByDate(userId, date);
//        return ResponseEntity.ok(todos);
//    }
//
//    @GetMapping("/schedule/{month}")
//    public ResponseEntity<List<TodoResponse>> getTodosByScheduledAndMonth(@PathVariable("month") LocalDate month, @RequestHeader("X-User-Id") Long userId) {
//        List<TodoResponse> todos = todoService.getTodosByScheduledAndMonth(userId, month);
//        return ResponseEntity.ok(todos);
//    }
//
    @Operation(operationId = "todoDetail", summary = "To-Do 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "To-Do 확인",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 Todo 접근)",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 To-Do",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class)))
    })
    @GetMapping("/detail/{id}")
    public ResponseEntity<TodoResponse> getTodoDetail(@PathVariable("id") Long id, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse todoResponse = todoService.getTodoById(userId, id);
        return ResponseEntity.ok(todoResponse);
    }
//
//    @GetMapping("/recur")
//    public ResponseEntity<List<TodoResponse>> getRecurTodos(@RequestHeader("X-User-Id") Long userId) {
//        List<TodoResponse> todos = todoService.getRecurTodos(userId);
//        return ResponseEntity.ok(todos);
//    }
//
//    @PatchMapping("/patch/{id}")
//    public ResponseEntity<TodoResponse> updateTodo(@PathVariable("id") Long id, @RequestBody TodoUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
//        TodoResponse updated = todoService.updateTodo(userId, id, request);
//        return ResponseEntity.ok(updated);
//    }
//
//    @PatchMapping("/patch/schedule/{id}")
//    public ResponseEntity<TodoResponse> updateSchedule(@PathVariable("id") Long id, @RequestBody ScheduleUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
//        TodoResponse updated = scheduleService.updateSchedule(userId, id, request);
//        return ResponseEntity.ok(updated);
//    }
//
//    @PatchMapping("/patch/repeat/{id}")
//    public ResponseEntity<TodoResponse> updateRepeatTodo(@PathVariable("id") Long id, @RequestBody RepeatTodoUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
//        TodoResponse updated = todoService.updateRepeatTodo(userId, id, request);
//        return ResponseEntity.ok(updated);
//    }
//
    @Operation(operationId = "todoCreate", summary = "To-Do 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "To-Do 확인",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
    })
    @PostMapping("/create/todo")
    public ResponseEntity<TodoResponse> createTodo(@RequestBody TodoCreateRequest todoCreateRequest, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse todoResponse = todoService.createTodo(userId, todoCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(todoResponse);
    }
//
//    @PostMapping("/create/schedule")
//    public ResponseEntity<TodoResponse> createSchedule(@RequestBody ScheduleRequest scheduleRequest, @RequestHeader("X-User-Id") Long userId) {
//        TodoResponse todoResponse = scheduleService.createSchedule(userId, scheduleRequest);
//        return ResponseEntity.ok(todoResponse);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTodo(@PathVariable("id") Long id, @RequestHeader("X-User-Id") Long userId) {
//        todoService.deleteTodo(userId, id);
//        return ResponseEntity.noContent().build();
//    }

}
