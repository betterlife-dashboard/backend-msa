package com.betterlife.focus.repository;

import com.betterlife.focus.domain.FocusTimer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FocusTimerRepository extends JpaRepository<FocusTimer, Long> {
    Optional<FocusTimer> getFocusTimerByUserId(Long userId);
}
