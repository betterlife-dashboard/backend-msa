package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoType;
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

}