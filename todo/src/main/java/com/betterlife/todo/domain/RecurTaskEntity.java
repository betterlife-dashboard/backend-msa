package com.betterlife.todo.domain;

import com.betterlife.todo.enums.RepeatType;
import com.betterlife.todo.enums.TodoType;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Table(
        name = "recur_tasks",
        indexes = {
                @Index(name = "idx_recur_tasks_user", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "todo_type", nullable = false)
    private TodoType type;

    @Column(name = "is_all_day", nullable = false)
    private boolean allDay = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType;

    @Column(name = "repeat_interval", nullable = false)
    private Byte repeatInterval = 1;

    @Column(name = "weekly_mask")
    private Byte weeklyMask;

    @Column(name = "monthly_day")
    private Byte monthlyDay;

    @Column(name = "at_time")
    private LocalTime atTime;

    @Column(name = "reminder_mask", nullable = false)
    private Byte reminderMask = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "active_from")
    private LocalDate activeFrom;

    @Column(name = "is_calendar", nullable = false)
    private boolean calendar = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public RecurTaskEntity(
            Long userId,
            String title,
            TodoType type,
            boolean allDay,
            RepeatType repeatType,
            Byte repeatInterval,
            Byte weeklyMask,
            Byte monthlyDay,
            LocalTime atTime,
            Byte reminderMask,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate activeFrom,
            boolean calendar
    ) {
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.allDay = allDay;
        this.repeatType = repeatType;
        this.repeatInterval = repeatInterval;
        this.weeklyMask = weeklyMask;
        this.monthlyDay = monthlyDay;
        this.atTime = atTime;
        this.reminderMask = reminderMask;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activeFrom = activeFrom;
        this.calendar = calendar;
    }

    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    public void changeTime(LocalTime newTime) {
        if (newTime == null) {
            this.allDay = true;
            this.atTime = null;
        } else {
            this.allDay = false;
            this.atTime = newTime;
        }
    }

    public void changeRepeat(RepeatType newRepeatType, Byte newInterval, Byte newRepeat) {
        if (newInterval == null || newInterval < 1) throw new IllegalArgumentException("interval must be >= 1");
        this.repeatType = newRepeatType;
        this.repeatInterval = newInterval;

        this.weeklyMask = null;
        this.monthlyDay = null;
        if (newRepeatType == RepeatType.WEEKLY) {
            if (newRepeat == null) {
                throw new IllegalArgumentException("weeklyMask required");
            }
            this.weeklyMask = newRepeat;
        } else if (newRepeatType == RepeatType.MONTHLY) {
            if (newRepeat == null) {
                throw new IllegalArgumentException("monthlyDay required");
            }
            this.monthlyDay = newRepeat;
        }
    }

    public void changeReminder(Byte newReminderMask) {
        this.reminderMask = newReminderMask;
    }

    public void changeRepeatDuration(LocalDate newStartDate, LocalDate newEndDate) {
        this.startDate = newStartDate;
        this.endDate = newEndDate;
    }

    public void changeActiveFrom(LocalDate newActiveFrom) {
        this.activeFrom = newActiveFrom;
    }

    public void onCalendar(boolean newCalendar) {
        this.calendar = newCalendar;
    }
}