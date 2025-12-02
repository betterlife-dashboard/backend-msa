package com.betterlife.notify.consumer;

import com.betterlife.notify.event.TodoEvent;
import com.betterlife.notify.service.NotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class NotifyConsumer {

    private final ObjectMapper objectMapper;
    private final NotifyService notifyService;

    @RabbitListener(queues = "${rabbit.deadline.queue.name}")
    public void consumeTodoDeadline(String message) {
        try {
            TodoEvent event = objectMapper.readValue(message, TodoEvent.class);
            if (event.getEventType().equals("create")) {
                notifyService.createDeadlineNotification(event);
            } else if (event.getEventType().equals("delete-todo")) {
                notifyService.deleteTodoNotification(event);
            } else if (event.getEventType().equals("delete-user")) {
                notifyService.deleteUserNotification(event);
            }
        } catch (Exception e) {
            System.err.println("e = " + e);
        }
    }

    @RabbitListener(queues = "${rabbit.reminder.queue.name}")
    public void consumeTodoReminder(String message) {
        try {
            TodoEvent event = objectMapper.readValue(message, TodoEvent.class);
            if (event.getEventType().equals("create")) {
                notifyService.createReminderNotification(event);
            } else if (event.getEventType().equals("delete-todo")) {
                notifyService.deleteTodoNotification(event);
            } else if (event.getEventType().equals("delete-user")) {
                notifyService.deleteUserNotification(event);
            }
        } catch (Exception e) {
            System.err.println("e = " + e);
        }
    }
}
