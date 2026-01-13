package com.betterlife.todo.scheduler;

import com.betterlife.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TodoScheduler {

//    private final TodoService todoService;
//
//    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//    public void generateDailyRecurringTodos() {
//        todoService.generateRecurringTodos();
//    }
//
//    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
//    public void closePastTodos() { todoService.closePastTodos(); }
}
