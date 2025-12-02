package com.betterlife.todo.producer;

import com.betterlife.todo.event.TodoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProducer {

    @Value("${rabbit.exchanges.todo}")
    private String todoExchangeName;

    @Value("${rabbit.deadline.queue.key}")
    private String deadlineKey;

    @Value("${rabbit.reminder.queue.key}")
    private String reminderKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendDeadline(TodoEvent todoEvent) {
        rabbitTemplate.convertAndSend(todoExchangeName, deadlineKey, todoEvent);
    }

    public void sendReminder(TodoEvent todoEvent) {
        rabbitTemplate.convertAndSend(todoExchangeName, reminderKey, todoEvent);
    }

}
