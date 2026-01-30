package com.betterlife.todo.event;

import com.betterlife.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final TodoService todoService;

    /**
     * Queue에 들어온 메시지를 구독
     */
    @RabbitListener(queues = "${rabbitmq.user-deleted.queue}")
    public void receiveMessage(UserDeletedEvent event) {
        log.info(
                "user_deleted_event_receive userId={}",
                event.getId()
        );
        todoService.deleteUser(event.getId());
    }
}
