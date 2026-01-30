package com.betterlife.todo.controller;

import com.betterlife.todo.dto.*;
import com.betterlife.todo.service.ScheduleService;
import com.betterlife.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
    @Parameter(
            in = ParameterIn.HEADER,
            name = "X-User-Id",
            required = true,
            schema = @Schema(type = "integer", defaultValue = "-1", example = "-1")
    )
    @GetMapping("/detail/{id}")
    public ResponseEntity<TodoResponse> getTodoDetail(@PathVariable("id") Long id, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse todoResponse = todoService.getTodoById(userId, id);
        return ResponseEntity.ok(todoResponse);
    }

    @Operation(operationId = "todoCreate", summary = "To-Do 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "To-Do 생성",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
    })
    @Parameter(
            in = ParameterIn.HEADER,
            name = "X-User-Id",
            required = true,
            schema = @Schema(type = "integer", defaultValue = "-1", example = "-1")
    )
    @PostMapping("/create/todo")
    public ResponseEntity<TodoResponse> createTodo(@RequestBody TodoCreateRequest todoCreateRequest, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse todoResponse = todoService.createTodo(userId, todoCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(todoResponse);
    }

    @Operation(operationId = "todoDelete", summary = "To-Do 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "To-Do 삭제 완료",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
            @ApiResponse(responseCode = "403", description = "허용되지 않은 To-Do 접근",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))),
    })
    @Parameter(
            in = ParameterIn.HEADER,
            name = "X-User-Id",
            required = true,
            schema = @Schema(type = "integer", defaultValue = "-1", example = "-1")
    )
    @DeleteMapping("/delete/todo/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("id") Long id, @RequestHeader("X-User-Id") Long userId) {
        todoService.deleteTodo(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/put/todo/{id}")
    public ResponseEntity<TodoResponse> updateTodo(@PathVariable("id") Long id, @RequestBody TodoUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
        TodoResponse updated = todoService.updateTodo(userId, id, request);
        return ResponseEntity.ok(updated);
    }

//    @GetMapping("/recur")
//    public ResponseEntity<List<TodoResponse>> getRecurTodos(@RequestHeader("X-User-Id") Long userId) {
//        List<TodoResponse> todos = todoService.getRecurTodos(userId);
//        return ResponseEntity.ok(todos);
//    }
//
//    @PatchMapping("/patch/repeat/{id}")
//    public ResponseEntity<TodoResponse> updateRepeatTodo(@PathVariable("id") Long id, @RequestBody RepeatTodoUpdateRequest request, @RequestHeader("X-User-Id") Long userId) {
//        TodoResponse updated = todoService.updateRepeatTodo(userId, id, request);
//        return ResponseEntity.ok(updated);
//    }
}
