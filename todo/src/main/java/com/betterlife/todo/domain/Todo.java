package com.betterlife.todo.domain;

import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title;

    @Enumerated(EnumType.STRING)
    private TodoType type = TodoType.GENERAL;

    @Enumerated(EnumType.STRING)
    private TodoStatus status = TodoStatus.PLANNED;

    private Boolean isRecurring = false;

    private Integer repeatDays = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_todo_id")
    private Todo parentTodo = null;

    @OneToMany(mappedBy = "parentTodo", cascade = CascadeType.ALL)
    private List<Todo> childTodos = new ArrayList<>();

    private LocalDateTime activeFrom;

    private LocalDateTime activeUntil;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private Timestamp updatedAt;

    @Builder
    public Todo(Long userId,
                String title,
                TodoType type,
                TodoStatus status,
                Integer repeatDays,
                LocalDateTime activeFrom,
                LocalDateTime activeUntil) {
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.status = status;
        this.repeatDays = repeatDays;
        this.isRecurring = (repeatDays != 0);
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
    }

    public void addChildTodo(Todo child) {
        this.childTodos.add(child);
        child.setParentTodo(this);
    }

    private void setParentTodo(Todo parent) {
        this.parentTodo = parent;
    }

    public void removeChildTodo(Todo child) {
        this.childTodos.remove(child);
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeRepeatDays(Integer repeatDays) {
        this.repeatDays = repeatDays;
        this.isRecurring = (repeatDays != 0);
    }

    public void changeType(TodoType type) {
        this.type = type;
    }

    public void updateStatus(TodoStatus status) {
        this.status = status;
    }

    public void changeActiveDate(LocalDateTime from, LocalDateTime until) {
        this.activeFrom = from;
        this.activeUntil = until;
    }
}
