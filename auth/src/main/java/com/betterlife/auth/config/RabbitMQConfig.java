package com.betterlife.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${rabbitmq.user-deleted.exchange}")
    private String deletedUserExchangeName;

    @Bean
    public DirectExchange deletedUserExchange() {
        return new DirectExchange(deletedUserExchangeName);
    }

    /**
     *
     * rabbitTemplate에 커스텀 기능 추가
     * - 메시지 컨버터를 통해 json을 일반 string 형태로 변경하여 전송
     * - 콜백을 받도록 하여 nack를 받은 경우 재시도
     * - 반환값을 받도록 하여 반환이 될 경우 에러로 여기고 로그 처리 -> 재시도 없음
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            String id = correlationData != null ? correlationData.getId() : "null";
            if (ack) {
                log.info("event_publish_confirm ack=true correlationId={}", id);
            } else {
                log.error("event_publish_confirm ack=false correlationId={} cause={}", id, cause);
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(() -> {
                    rabbitTemplate.convertAndSend(
                            correlationData.getReturned().getExchange(),
                            correlationData.getReturned().getRoutingKey(),
                            correlationData.getReturned().getMessage().getBody(),
                            message -> {
                                message.getMessageProperties().setMessageId(correlationData.getId());
                                return message;
                            },
                            correlationData
                    );
                }, 200, TimeUnit.MILLISECONDS);
            }
        });

        // 큐 자체에 도달하지 못한 경우이므로 재시도도 의미가 없을 가능성이 크다. 따라서 이 경우에는 로그만 남겨서 코드 자체 수정을 유도한다.
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error(
                    "event_publish_return replyCode={} replyText={} exchange={} routingKey={} messageId={}",
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getMessage().getMessageProperties().getMessageId()
            );
        });

        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
