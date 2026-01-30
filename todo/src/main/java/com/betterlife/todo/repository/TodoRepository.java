package com.betterlife.todo.repository;

import com.betterlife.todo.domain.TodoEntity;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    List<TodoEntity> findAllByUserId(Long userId);
    Optional<TodoEntity> findByTitle(String title);
    void deleteAllByUserId(Long userId);

//    List<TodoEntity> findAllByUserIdAndActiveFromBeforeAndActiveUntilAfter(
//            Long userId,
//            LocalDateTime activeFromBefore,
//            LocalDateTime activeUntilAfter
//    );
//
//    List<TodoEntity> findAllByUserIdAndTypeAndActiveFromBeforeAndActiveUntilAfter(
//            Long userId,
//            TodoType type,
//            LocalDateTime activeFromBefore,
//            LocalDateTime activeUntilAfter
//    );
//
//
//    List<TodoEntity> findAllByUserIdAndIsRecurring(
//            Long userId,
//            Boolean isRecurring
//    );
//
//    List<TodoEntity> findAllByIsRecurring(Boolean isRecurring);
//
//    List<TodoEntity> findAllByStatusAndActiveUntilBefore(
//            TodoStatus status,
//            LocalDateTime activeUntilBefore
//    );
//
}
