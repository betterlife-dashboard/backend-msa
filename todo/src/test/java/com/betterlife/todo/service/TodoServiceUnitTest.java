package com.betterlife.todo.service;

import com.betterlife.todo.client.UserClient;
import com.betterlife.todo.domain.Todo;
import com.betterlife.todo.dto.TodoRequest;
import com.betterlife.todo.enums.TodoStatus;
import com.betterlife.todo.enums.TodoType;
import com.betterlife.todo.event.ScheduleDeletedEvent;
import com.betterlife.todo.exception.AccessDeniedException;
import com.betterlife.todo.exception.InvalidRequestException;
import com.betterlife.todo.message.EventProducer;
import com.betterlife.todo.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceUnitTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    void getTodoById_deniesOtherUser() {
        Todo todo = Todo.builder()
                .userId(1L)
                .title("title")
                .type(TodoType.GENERAL)
                .status(TodoStatus.PLANNED)
                .repeatDays(0)
                .build();
        ReflectionTestUtils.setField(todo, "id", 10L);

        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.getTodoById(2L, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createTodo_invalidDate_throws() {
        TodoRequest request = TodoRequest.builder()
                .title("bad")
                .type(TodoType.GENERAL)
                .repeatDays(0)
                .activeFrom(LocalDateTime.now().plusDays(1))
                .activeUntil(LocalDateTime.now())
                .build();

        doNothing().when(userClient).getUser(1L);

        assertThatThrownBy(() -> todoService.createTodo(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void deleteTodo_scheduleSendsEvent() {
        Todo todo = Todo.builder()
                .userId(1L)
                .title("title")
                .repeatDays(0)
                .type(TodoType.SCHEDULE)
                .status(TodoStatus.PLANNED)
                .build();
        ReflectionTestUtils.setField(todo, "id", 9L);

        when(todoRepository.findById(9L)).thenReturn(Optional.of(todo));

        todoService.deleteTodo(1L, 9L);

        ArgumentCaptor<ScheduleDeletedEvent> captor = ArgumentCaptor.forClass(ScheduleDeletedEvent.class);
        verify(eventProducer).sendScheduleDeletedEvent(captor.capture());
        assertThat(captor.getValue().getTodoId()).isEqualTo(9L);
        verify(todoRepository).deleteById(9L);
    }
}
