package com.betterlife.focus.controller;

import com.betterlife.focus.dto.TimerRequest;
import com.betterlife.focus.dto.TimerResponse;
import com.betterlife.focus.service.FocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/focus")
public class FocusController {

    private final FocusService focusService;

    @PostMapping("/timer")
    public ResponseEntity<TimerResponse> getTimer(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(focusService.getTimer(userId));
    }

    @PutMapping("/timer")
    public ResponseEntity<Void> controlTimer(@RequestBody TimerRequest timerRequest, @RequestHeader("X-User-Id") Long userId) {
        focusService.controlTimer(timerRequest, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/timer")
    public ResponseEntity<Void> endTimer(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.noContent().build();
    }

}
