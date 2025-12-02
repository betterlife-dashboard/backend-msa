package com.betterlife.notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitConfig {

    @Value("${rabbit.exchanges.todo}")
    private String todoExchangeName;

    @Value("${rabbit.deadline.queue.name}")
    private String todoDeadlineQueue;

    @Value("${rabbit.reminder.queue.name}")
    private String todoReminderName;

    @Value("${rabbit.deadline.queue.key}")
    private String todoDeadlineKey;

    @Value("${rabbit.reminder.queue.key}")
    private String todoReminderKey;

    @Bean
    public Queue todoDeadlineQueue() {
        return new Queue(todoDeadlineQueue);
    }

    @Bean
    public Binding todoDeadlineBinding() {
        return BindingBuilder
                .bind(todoDeadlineQueue())
                .to(new DirectExchange(todoExchangeName))
                .with(todoDeadlineKey);
    }

    @Bean
    public Queue todoReminderQueue() {
        return new Queue(todoReminderName);
    }

    @Bean
    public Binding todoReminderBinding() {
        return BindingBuilder
                .bind(todoReminderQueue())
                .to(new DirectExchange(todoExchangeName))
                .with(todoReminderKey);
    }
}
