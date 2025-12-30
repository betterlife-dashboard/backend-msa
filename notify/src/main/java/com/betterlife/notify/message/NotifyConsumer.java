package com.betterlife.notify.message;

import com.betterlife.notify.event.ScheduleCreatedEvent;
import com.betterlife.notify.event.ScheduleDeletedEvent;
import com.betterlife.notify.event.UserDeletedEvent;
import com.betterlife.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifyConsumer {

    private final NotifyService notifyService;

    @RabbitListener(queues = "${rabbit.queue.schedule.created.name}")
    public void consumeScheduleCreatedEvent(ScheduleCreatedEvent event) {
        notifyService.createScheduleNotify(event);
    }

    @RabbitListener(queues = "${rabbit.queue.schedule.deleted.name}")
    public void consumeScheduleDeletedEvent(ScheduleDeletedEvent event) {
        notifyService.deleteScheduleNotify(event);
    }

    @RabbitListener(queues = "${rabbit.queue.user.deleted.name}")
    public void consumeUserDeletedEvent(UserDeletedEvent event) {
        notifyService.deleteUserNotify(event);
    }
}
