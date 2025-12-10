package com.betterlife.todo.dto;

import com.betterlife.todo.enums.TodoType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class RepeatTodoUpdateRequest {
    private String title;
    private TodoType type;
    private Integer repeatDays;

}