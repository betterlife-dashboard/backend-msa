package com.betterlife.todo.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.todo-updated.exchange}")
    private String todoUpdatedExchangeName;

    @Value("${rabbitmq.todo-updated.key}")
    private String todoUpdatedKey;

    @Value("${rabbitmq.todo-deleted.exchange}")
    private String todoDeletedExchangeName;

    @Value("${rabbitmq.todo-deleted.key}")
    private String todoDeletedKey;

    public void sendTodoUpdatedEvent(Long todoId, Byte reminderMask) {
        TodoUpdatedEvent todoUpdatedEvent = new TodoUpdatedEvent(todoId, reminderMask);

        CorrelationData cd = new CorrelationData("userUpdated:" + todoId + ":" + UUID.randomUUID());

        rabbitTemplate.convertAndSend(
                todoUpdatedExchangeName,
                todoUpdatedKey,
                todoUpdatedEvent,
                message -> {
                    message.getMessageProperties().setMessageId(cd.getId());
                    return message;
                },
                cd
        );
    }

    public void sendTodoDeletedEvent(Long todoId) {
        TodoDeletedEvent todoDeletedEvent = new TodoDeletedEvent(todoId);

        CorrelationData cd = new CorrelationData("userDeleted:" + todoId + ":" + UUID.randomUUID());

        rabbitTemplate.convertAndSend(
                todoDeletedExchangeName,
                todoDeletedKey,
                todoDeletedEvent,
                message -> {
                    message.getMessageProperties().setMessageId(cd.getId());
                    return message;
                },
                cd
        );
    }
}
