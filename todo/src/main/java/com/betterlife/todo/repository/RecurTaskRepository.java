package com.betterlife.todo.repository;

import com.betterlife.todo.domain.RecurTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurTaskRepository extends JpaRepository<RecurTaskEntity, Long> {
}
