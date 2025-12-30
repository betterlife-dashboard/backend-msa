package com.betterlife.notify.enums;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.UnaryOperator;

public enum RemainTimeRule {
    ONE_HOUR("1h", "1시간", d -> d.minusHours(1)),
    ONE_DAY("1d", "1시간", d -> d.minusDays(1)),
    THREE_DAYS("3d", "1시간", d -> d.minusDays(3)),
    ONE_WEEK("1w", "1시간", d -> d.minusWeeks(1));

    private final String code;
    @Getter private final String label;
    private final UnaryOperator<LocalDateTime> adjuster;

    RemainTimeRule(String code, String label, UnaryOperator<LocalDateTime> adjuster) {
        this.code = code;
        this.label = label;
        this.adjuster = adjuster;
    }

    public static RemainTimeRule fromCode(String code) {
        return Arrays.stream(values())
                .filter(r -> r.code.equals(code))
                .findFirst()
                .orElseThrow();
    }

    public LocalDateTime apply(LocalDateTime d) {
        return adjuster.apply(d);
    }
}
