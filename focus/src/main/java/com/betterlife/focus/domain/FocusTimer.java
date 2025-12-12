package com.betterlife.focus.domain;

import com.betterlife.focus.dto.TimerRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "focus_timers")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FocusTimer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private Boolean isActive;

    private Long duration;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public void controlTimer(TimerRequest timerRequest) {
        this.name = timerRequest.getName();
        this.isActive = timerRequest.getIsActive();
        this.duration = timerRequest.getDuration();
        this.startAt = timerRequest.getStartAt();
        this.endAt = timerRequest.getEndAt();
    }



    @Builder
    public FocusTimer(Long userId, String name, Boolean isActive, Long duration, LocalDateTime startAt, LocalDateTime endAt) {
        this.userId = userId;
        this.name = name;
        this.isActive = isActive;
        this.duration = duration;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
