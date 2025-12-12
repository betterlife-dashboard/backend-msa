package com.betterlife.focus.service;

import com.betterlife.focus.domain.FocusTimer;
import com.betterlife.focus.dto.TimerRequest;
import com.betterlife.focus.dto.TimerResponse;
import com.betterlife.focus.repository.FocusTimerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FocusService {

    private final FocusTimerRepository focusTimerRepository;

    @Transactional
    public TimerResponse getTimer(Long userId) {
        FocusTimer focusTimer = focusTimerRepository.getFocusTimerByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("timer가 생성되지 않았습니다."));
        /*
            TODO: 만약 focusTimer의 isActive가 true일 때, 현재 시각이 endAt을 넘었다면
            TODO: focusTimer의 isActive를 false로 만들고, 그 시각만큼 todo에 추가한다 -> endTimer 사용
        */
        return TimerResponse.fromEntity(focusTimer);
    }

    @Transactional
    public void controlTimer(TimerRequest timerRequest, Long userId) {
        Optional<FocusTimer> focusTimerOptional = focusTimerRepository.getFocusTimerByUserId(userId);
        if (focusTimerOptional.isEmpty()) {
            FocusTimer focusTimer = FocusTimer.builder()
                    .userId(userId)
                    .name("")
                    .isActive(false)
                    .duration(0L)
                    .startAt(LocalDateTime.now())
                    .endAt(LocalDateTime.now())
                    .build();
            FocusTimer saved = focusTimerRepository.save(focusTimer);
        } else {
            FocusTimer focusTimer = focusTimerOptional.get();
            focusTimer.controlTimer(timerRequest);
        }
    }

    public void endTimer(Long id) {

    }

}
