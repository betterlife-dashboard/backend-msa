package com.betterlife.todo.message;

import com.betterlife.todo.event.ScheduleCreatedEvent;
import com.betterlife.todo.event.ScheduleDeletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String notifyExchange;
    private final String scheduleCreatedQueueKey;
    private final String scheduleDeletedQueueKey;
    private final String userDeletedQueueKey;

    public EventProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbit.exchanges.notify}") String notifyExchange,
            @Value("${rabbit.queue.schedule.created.key}") String scheduleCreatedQueueKey,
            @Value("${rabbit.queue.schedule.deleted.key}") String scheduleDeletedQueueKey,
            @Value("${rabbit.queue.user.deleted.key}") String userDeletedQueueKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.notifyExchange = notifyExchange;
        this.scheduleCreatedQueueKey = scheduleCreatedQueueKey;
        this.scheduleDeletedQueueKey = scheduleDeletedQueueKey;
        this.userDeletedQueueKey = userDeletedQueueKey;
    }

    public void sendScheduleCreatedEvent(ScheduleCreatedEvent scheduleCreatedEvent) {
        rabbitTemplate.convertAndSend(notifyExchange, scheduleCreatedQueueKey, scheduleCreatedEvent);
    }

    public void sendScheduleDeletedEvent(ScheduleDeletedEvent scheduleDeletedEvent) {
        rabbitTemplate.convertAndSend(notifyExchange, scheduleDeletedQueueKey, scheduleDeletedEvent);
    }

}
