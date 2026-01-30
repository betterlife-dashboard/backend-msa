package com.betterlife.auth.event;

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

    @Value("${rabbitmq.user-deleted.exchange}")
    private String userDeletedExchange;

    @Value("${rabbitmq.user-deleted.key}")
    private String userDeletedKey;

    public void sendUserDeletedEvent(Long userId) {
        UserDeletedEvent userDeletedEvent = new UserDeletedEvent(userId);

        CorrelationData cd = new CorrelationData("userDeleted:" + userId + ":" + UUID.randomUUID());

        rabbitTemplate.convertAndSend(
                userDeletedExchange,
                userDeletedKey,
                userDeletedEvent,
                message -> {
                    message.getMessageProperties().setMessageId(cd.getId());
                    return message;
                },
                cd
        );
    }
}
