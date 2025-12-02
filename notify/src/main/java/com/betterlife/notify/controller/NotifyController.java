package com.betterlife.notify.controller;

import com.betterlife.notify.dto.NotifyResponse;
import com.betterlife.notify.dto.TodoNotifyDetailResponse;
import com.betterlife.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping("/{todo-id}")
    public ResponseEntity<TodoNotifyDetailResponse> getNotifyByTodoId(@PathVariable("todo-id") Long todoId, @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notifyService.getNotificationByTodoId(userId, todoId));
    }

    @GetMapping("/now")
    public ResponseEntity<List<NotifyResponse>> getNotifyNow(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notifyService.getNotificationsNow(userId));
    }
}
