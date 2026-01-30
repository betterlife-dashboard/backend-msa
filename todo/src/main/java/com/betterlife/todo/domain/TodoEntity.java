package com.betterlife.todo.domain;

import com.betterlife.todo.dto.TodoUpdateRequest;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.*;
import java.util.*;

@Entity
@Table(
        name = "todos",
        indexes = {
                @Index(name = "idx_todos_user", columnList = "user_id"),
                @Index(name = "idx_todos_user_occurrence", columnList = "user_id,occurrence_date"),
                @Index(name = "idx_todos_user_status", columnList = "user_id,todo_status"),
                @Index(name = "idx_todos_recur", columnList = "recur_task_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_todos_recur_occurrence",
                        columnNames = {"recur_task_id", "occurrence_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
            name = "recur_task_id",
            foreignKey = @ForeignKey(name = "fk_todos_recur_task")
    )
    private RecurTaskEntity recurTask;

    @Column(name = "recur_task_id", insertable = false, updatable = false)
    private Long recurTaskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "todo_type", nullable = false)
    private TodoType todoType = TodoType.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "todo_status", nullable = false)
    private TodoStatus todoStatus = TodoStatus.PENDING;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "is_all_day", nullable = false)
    private boolean allDay;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @Column(name = "at_time")
    private LocalTime atTime;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_sec")
    private Integer durationSec;

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
    public TodoEntity(
            Long userId,
            RecurTaskEntity recurTask,
            TodoType todoType,
            TodoStatus todoStatus,
            String title,
            String memo,
            boolean allDay,
            LocalDate occurrenceDate,
            LocalTime atTime,
            LocalDateTime completedAt,
            Integer durationSec
    ) {
        this.userId = userId;
        this.recurTask = recurTask;
        this.todoType = todoType;
        this.todoStatus = todoStatus;
        this.title = title;
        this.memo = memo;
        this.allDay = allDay;
        this.occurrenceDate = occurrenceDate;
        this.atTime = atTime;
        this.completedAt = completedAt;
        this.durationSec = durationSec;
    }

    public void repending() {
        this.todoStatus = TodoStatus.PENDING;
        this.completedAt = null;
    }

    public void done() {
        this.todoStatus = TodoStatus.DONE;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.todoStatus = TodoStatus.CANCELLED;
        this.completedAt = null;
    }

    public void update(TodoUpdateRequest request) {
        this.todoStatus = request.getTodoStatus();
        this.title = request.getTitle();
        this.memo = request.getMemo();
        changeTime(request.isAllDay(), request.getOccurrenceDate(), request.getAtTime());
    }

    public void changeTime(boolean newAllDay, LocalDate newOccurrenceDate, LocalTime newAtTime) {
        this.allDay = newAllDay;
        if (newOccurrenceDate == null) {
            throw new IllegalArgumentException("occurrenceDate required");
        }
        this.occurrenceDate = newOccurrenceDate;

        if (newAllDay) {
            this.atTime = null;
        } else {
            this.atTime = Objects.requireNonNull(newAtTime);
        }
    }

    public void addDurationSec(Integer newDurationSec) {
        if (this.durationSec == null) this.durationSec = 0;
        this.durationSec += newDurationSec;
    }
}