package com.betterlife.notify.controller;

import com.betterlife.notify.domain.FcmToken;
import com.betterlife.notify.dto.FcmTokenRequest;
import com.betterlife.notify.dto.FcmTokenResponse;
import com.betterlife.notify.dto.WebNotify;
import com.betterlife.notify.service.FcmService;
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
    private final FcmService fcmService;

    @GetMapping("")
    public ResponseEntity<List<WebNotify>> getNotifies(
            @RequestParam("todo-id") Long todoId
    ) {
        return ResponseEntity.ok(notifyService.getNotifies(todoId));
    }

    @GetMapping("/token")
    public ResponseEntity<FcmTokenResponse> getFcmToken(
            @RequestParam("device-type") String deviceType,
            @RequestParam("browser-type") String browserType,
            @RequestHeader("X-User-Id") Long id) {
        return ResponseEntity.ok(fcmService.getFcmToken(id, deviceType, browserType));
    }

    @PostMapping("/token")
    public ResponseEntity<FcmTokenResponse> refreshFcmToken(@RequestBody FcmTokenRequest request, @RequestHeader("X-User-Id") Long id) {
        return ResponseEntity.ok(fcmService.saveFcmToken(id, request));
    }
}
