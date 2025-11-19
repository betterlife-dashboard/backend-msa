package com.betterlife.todo.repository;

import com.betterlife.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByUserId(Long userId);
    Optional<Todo> findByTitle(String title);

    @Query("""
    SELECT t
    FROM Todo t
    WHERE t.userId = :userId
    AND t.activeUntil >= :startOfDay
    AND t.activeFrom < :endOfDay
""")
    List<Todo> findAllByUserIdAndDateWithinActivePeriod(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );


    List<Todo> findAllByUserIdAndIsRecurring(
            Long userId,
            Boolean isRecurring
    );

    List<Todo> findAllByIsRecurring(Boolean isRecurring);

    List<Todo> findAllByTodoStatusPlannedAndActiveUntilBefore(
            LocalDateTime currentTime
    );
}
