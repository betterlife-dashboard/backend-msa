package com.betterlife.todo.integration;

import com.betterlife.todo.domain.TodoEntity;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import com.betterlife.todo.repository.TodoRepository;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TodoIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.todo-deleted.exchange}")
    private String todoDeletedExchange;

    @Value("${rabbitmq.todo-deleted.key}")
    private String todoDeletedKey;

    @Value("${rabbitmq.todo-updated.exchange}")
    private String todoUpdatedExchange;

    @Value("${rabbitmq.todo-updated.key}")
    private String todoUpdatedKey;

    @Test
    void create_todo_success_returns201_and_persists() throws Exception {
        todoRepository.deleteAll();
        String createJson = """
                {
                  "title": "Test To-Do",
                  "memo": "always be happy",
                  "allDay": true,
                  "occurrenceDate": "2026-01-15",
                  "atTime": "00:00:00"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/todo/create/todo")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test To-Do"));

        List<TodoEntity> allByUserId = todoRepository.findAllByUserId(1L);
        assertThat(allByUserId.size()).isEqualTo(1);
        assertThat(allByUserId.get(0).getTitle()).isEqualTo("Test To-Do");
    }

    @Test
    void detail_todo_success_returns200_and_expected_payload() throws Exception {
        todoRepository.deleteAll();
        TodoEntity saved = todoRepository.save(TodoEntity.builder()
                .userId(1L)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title("Detail Target")
                .memo("memo-text")
                .allDay(false)
                .occurrenceDate(LocalDate.of(2026, 1, 20))
                .atTime(LocalTime.of(9, 30))
                .completedAt(null)
                .durationSec(120)
                .build());

        mockMvc.perform(MockMvcRequestBuilders.get("/todo/detail/{id}", saved.getId())
                        .header("X-User-Id", 1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(saved.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Detail Target"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.memo").value("memo-text"));
    }

    @Test
    void detail_todo_forbidden_when_accessing_other_users_todo() throws Exception {
        todoRepository.deleteAll();
        TodoEntity saved = todoRepository.save(TodoEntity.builder()
                .userId(1L)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title("Owner Only")
                .memo("private")
                .allDay(true)
                .occurrenceDate(LocalDate.of(2026, 1, 25))
                .atTime(null)
                .completedAt(null)
                .durationSec(null)
                .build());

        mockMvc.perform(MockMvcRequestBuilders.get("/todo/detail/{id}", saved.getId())
                        .header("X-User-Id", 2))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이 Todo에 접근할 권한이 없습니다."));
    }

    @Test
    void delete_todo_success_publishes_event_and_returns204() throws Exception {
        todoRepository.deleteAll();
        TodoEntity saved = todoRepository.save(TodoEntity.builder()
                .userId(1L)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title("Delete Target")
                .memo("memo")
                .allDay(true)
                .occurrenceDate(LocalDate.of(2026, 1, 10))
                .atTime(null)
                .completedAt(null)
                .durationSec(null)
                .build());

        String queueName = declareTestQueue(todoDeletedExchange, todoDeletedKey);

        mockMvc.perform(MockMvcRequestBuilders.delete("/todo/delete/todo/{id}", saved.getId())
                        .header("X-User-Id", 1))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertThat(todoRepository.findById(saved.getId())).isEmpty();

        Object payload = rabbitTemplate.receiveAndConvert(queueName, 2000);
        assertThat(payload).isNotNull();
        assertThat(payload.toString()).contains(saved.getId().toString());
    }

    @Test
    void delete_todo_forbidden_when_accessing_other_users_todo() throws Exception {
        todoRepository.deleteAll();
        TodoEntity saved = todoRepository.save(TodoEntity.builder()
                .userId(1L)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title("Owner Only")
                .memo("private")
                .allDay(true)
                .occurrenceDate(LocalDate.of(2026, 1, 11))
                .atTime(null)
                .completedAt(null)
                .durationSec(null)
                .build());

        mockMvc.perform(MockMvcRequestBuilders.delete("/todo/delete/todo/{id}", saved.getId())
                        .header("X-User-Id", 2))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이 Todo에 접근할 권한이 없습니다."));
    }

    @Test
    void put_todo_success_publishes_event_and_returns200() throws Exception {
        todoRepository.deleteAll();
        TodoEntity saved = todoRepository.save(TodoEntity.builder()
                .userId(1L)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title("Before Update")
                .memo("memo")
                .allDay(true)
                .occurrenceDate(LocalDate.of(2026, 1, 12))
                .atTime(null)
                .completedAt(null)
                .durationSec(null)
                .build());

        String queueName = declareTestQueue(todoUpdatedExchange, todoUpdatedKey);

        String updateJson = """
                {
                  "todoStatus": "DONE",
                  "title": "After Update",
                  "memo": "updated memo",
                  "allDay": false,
                  "occurrenceDate": "2026-01-20",
                  "atTime": "09:45:00",
                  "reminderMask": 3
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/todo/put/todo/{id}", saved.getId())
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(saved.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.todoStatus").value("DONE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("After Update"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.memo").value("updated memo"));

        Object payload = rabbitTemplate.receiveAndConvert(queueName, 2000);
        assertThat(payload).isNotNull();
        assertThat(payload.toString()).contains(saved.getId().toString());
        assertThat(payload.toString()).contains("reminderMask=3");
    }

    @Test
    void put_todo_forbidden_when_accessing_other_users_todo() throws Exception {
        todoRepository.deleteAll();
        TodoEntity saved = todoRepository.save(TodoEntity.builder()
                .userId(1L)
                .recurTask(null)
                .todoType(TodoType.TODO)
                .todoStatus(TodoStatus.PENDING)
                .title("Owner Only")
                .memo("private")
                .allDay(true)
                .occurrenceDate(LocalDate.of(2026, 1, 13))
                .atTime(null)
                .completedAt(null)
                .durationSec(null)
                .build());

        String updateJson = """
                {
                  "todoStatus": "DONE",
                  "title": "Hacker Update",
                  "memo": "nope",
                  "allDay": true,
                  "occurrenceDate": "2026-01-21",
                  "atTime": null,
                  "reminderMask": 1
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/todo/put/todo/{id}", saved.getId())
                        .header("X-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("이 Todo에 접근할 권한이 없습니다."));
    }

    private String declareTestQueue(String exchangeName, String routingKey) {
        String queueName = "test.todo." + UUID.randomUUID();
        Queue queue = new Queue(queueName, false, true, true);
        DirectExchange exchange = new DirectExchange(exchangeName);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        amqpAdmin.declareBinding(binding);
        return queueName;
    }

}
