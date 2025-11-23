package com.betterlife.todo.repository;

import com.betterlife.todo.domain.Todo;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByUserId(Long userId);
    Optional<Todo> findByTitle(String title);

    List<Todo> findAllByUserIdAndActiveFromBeforeAndActiveUntilAfter(
            Long userId,
            LocalDateTime activeFromBefore,
            LocalDateTime activeUntilAfter
    );

    List<Todo> findAllByUserIdAndTypeAndActiveFromBeforeAndActiveUntilAfter(
            Long userId,
            TodoType type,
            LocalDateTime activeFromBefore,
            LocalDateTime activeUntilAfter
    );


    List<Todo> findAllByUserIdAndIsRecurring(
            Long userId,
            Boolean isRecurring
    );

    List<Todo> findAllByIsRecurring(Boolean isRecurring);

    List<Todo> findAllByStatusAndActiveUntilBefore(
            TodoStatus status,
            LocalDateTime activeUntilBefore
    );

    void deleteAllByUserId(Long userId);
}
