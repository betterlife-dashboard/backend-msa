package com.betterlife.notify.enums;

import java.util.Map;

public enum NotifyType {

    REMINDER(
            "리마인드: {title}",
            "{title} 시작 {timeLeft} 전입니다."
    ),

    DEADLINE(
            "데드라인: {title}",
            "{title} 마감 {timeLeft} 전입니다."
    );

    private final String titleTemplate;
    private final String bodyTemplate;

    NotifyType(String titleTemplate, String bodyTemplate) {
        this.titleTemplate = titleTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public static NotifyType from(String standard) {
        if (standard == null) {
            throw new IllegalArgumentException("standard is null");
        }

        return switch (standard.toLowerCase()) {
            case "reminder" -> REMINDER;
            case "deadline" -> DEADLINE;
            default -> throw new IllegalArgumentException(
                    "Unknown notify standard: " + standard
            );
        };
    }

    public String renderTitle(Map<String, String> context) {
        String title = titleTemplate;
        for (Map.Entry<String, String> e : context.entrySet()) {
            title = title.replace("{" + e.getKey() + "}", e.getValue());
        }
        return title;
    }

    public String renderBody(Map<String, String> context) {
        String body = bodyTemplate;
        for (Map.Entry<String, String> e : context.entrySet()) {
            body = body.replace("{" + e.getKey() + "}", e.getValue());
        }
        return body;
    }
}