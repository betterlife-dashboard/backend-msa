package com.betterlife.notify.scheduler;

import com.betterlife.notify.service.FcmService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final FcmService fcmService;

    @PostConstruct
    public void startWorker() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                try {
                    fcmService.popDueNotify();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                Thread.sleep(1000);
            }
        });
    }

}
