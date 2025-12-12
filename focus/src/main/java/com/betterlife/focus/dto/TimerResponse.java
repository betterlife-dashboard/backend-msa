package com.betterlife.focus.dto;

import com.betterlife.focus.domain.FocusTimer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class TimerResponse {

    private Long id;
    private String name;
    private Boolean isActive;
    private Long duration;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public static TimerResponse fromEntity(FocusTimer focusTimer) {
        TimerResponse timerResponse = new TimerResponse();
        timerResponse.id = focusTimer.getId();
        timerResponse.name = focusTimer.getName();
        timerResponse.isActive = focusTimer.getIsActive();
        timerResponse.duration = focusTimer.getDuration();
        timerResponse.startAt = focusTimer.getStartAt();
        timerResponse.endAt = focusTimer.getEndAt();
        return timerResponse;
    }
}
