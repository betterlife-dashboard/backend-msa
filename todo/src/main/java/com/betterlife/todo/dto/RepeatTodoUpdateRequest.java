package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RepeatTodoUpdateRequest {
    private String title;
    private TodoType type;
    private Integer repeatDays;

    @Builder
    public RepeatTodoUpdateRequest(String title,
                                   TodoType type,
                                   Integer repeatDays) {
        this.title = title;
        this.type = type;
        this.repeatDays = repeatDays;
    }
}
