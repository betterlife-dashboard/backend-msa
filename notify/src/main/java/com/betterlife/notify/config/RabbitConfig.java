package com.betterlife.notify.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private final String notifyExchange;
    private final String deadNotifyExchange;

    private final String scheduleCreatedQueueName;
    private final String scheduleCreatedQueueKey;

    private final String scheduleDeletedQueueName;
    private final String scheduleDeletedQueueKey;

    private final String userDeletedQueueName;
    private final String userDeletedQueueKey;

    private final String deadNotifyQueueName;
    private final String deadNotifyQueueKey;

    public RabbitConfig(
            @Value("${rabbit.exchanges.notify}") String notifyExchange,
            @Value("${rabbit.exchanges.dead}") String deadNotifyExchange,
            @Value("${rabbit.queue.schedule.created.name}") String scheduleCreatedQueueName,
            @Value("${rabbit.queue.schedule.created.key}") String scheduleCreatedQueueKey,
            @Value("${rabbit.queue.schedule.deleted.name}") String scheduleDeletedQueueName,
            @Value("${rabbit.queue.schedule.deleted.key}") String scheduleDeletedQueueKey,
            @Value("${rabbit.queue.user.deleted.name}") String userDeletedQueueName,
            @Value("${rabbit.queue.user.deleted.key}") String userDeletedQueueKey,
            @Value("${rabbit.queue.notify.dead.name}") String deadNotifyQueueName,
            @Value("${rabbit.queue.notify.dead.key}") String deadNotifyQueueKey) {
        this.notifyExchange = notifyExchange;
        this.deadNotifyExchange = deadNotifyExchange;
        this.scheduleCreatedQueueName = scheduleCreatedQueueName;
        this.scheduleCreatedQueueKey = scheduleCreatedQueueKey;
        this.scheduleDeletedQueueName = scheduleDeletedQueueName;
        this.scheduleDeletedQueueKey = scheduleDeletedQueueKey;
        this.userDeletedQueueName = userDeletedQueueName;
        this.userDeletedQueueKey = userDeletedQueueKey;
        this.deadNotifyQueueName = deadNotifyQueueName;
        this.deadNotifyQueueKey = deadNotifyQueueKey;
    }

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(notifyExchange, true, false);
    }

    @Bean
    public DirectExchange deadNotifyExchange() {
        return new DirectExchange(deadNotifyExchange, true, false);
    }

    @Bean
    public Queue scheduleCreatedQueue() {
        return QueueBuilder.durable(scheduleCreatedQueueName)
                .withArgument("x-dead-letter-exchange", deadNotifyExchange)
                .withArgument("x-dead-letter-routing-key", deadNotifyQueueKey)
                .build();
    }

    @Bean
    public Queue scheduleDeletedQueue() {
        return QueueBuilder.durable(scheduleDeletedQueueName)
                .withArgument("x-dead-letter-exchange", deadNotifyExchange)
                .withArgument("x-dead-letter-routing-key", deadNotifyQueueKey)
                .build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(userDeletedQueueName)
                .withArgument("x-dead-letter-exchange", deadNotifyExchange)
                .withArgument("x-dead-letter-routing-key", deadNotifyQueueKey)
                .build();
    }

    @Bean
    public Queue deadNotifyQueue() {
        return QueueBuilder.durable(deadNotifyQueueName).build();
    }

    @Bean
    public Binding scheduleCreatedBinding() {
        return BindingBuilder
                .bind(scheduleCreatedQueue())
                .to(notifyExchange())
                .with(scheduleCreatedQueueKey);
    }

    @Bean
    public Binding scheduleDeletedBinding() {
        return BindingBuilder
                .bind(scheduleDeletedQueue())
                .to(notifyExchange())
                .with(scheduleDeletedQueueKey);
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder
                .bind(userDeletedQueue())
                .to(notifyExchange())
                .with(userDeletedQueueKey);
    }

    @Bean
    public Binding deadNotifyBinding() {
        return BindingBuilder
                .bind(deadNotifyQueue())
                .to(deadNotifyExchange())
                .with(deadNotifyQueueKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter jacksonJsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonJsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, JacksonJsonMessageConverter jacksonJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setDefaultRequeueRejected(false);
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        return factory;
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
