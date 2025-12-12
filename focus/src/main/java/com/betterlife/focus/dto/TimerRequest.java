package com.betterlife.focus.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TimerRequest {

    private String name;
    private Boolean isActive;
    private Long duration;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
