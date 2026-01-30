package com.betterlife.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class TodoCreateRequest {

    @Schema(example = "Test To-Do")
    @NotBlank
    private String title;

    @Schema(example = "always be happy")
    @NotBlank
    private String memo;

    @Schema(example = "true")
    private boolean allDay;

    @Schema(example = "2026-01-15")
    private LocalDate occurrenceDate;

    @Schema(type = "string", format = "time", example = "00:00:00", nullable = true)
    private LocalTime atTime;

    @Builder
    public TodoCreateRequest(
            String title,
            String memo,
            boolean allDay,
            LocalDate occurrenceDate,
            LocalTime atTime
    ) {
        this.title = title;
        this.memo = memo;
        this.allDay = allDay;
        this.occurrenceDate = occurrenceDate;
        this.atTime = atTime;
    }
}
