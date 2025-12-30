package com.betterlife.notify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class WebMessageDto {
    private Message message;

    @Data
    @Builder
    public static class Message {
        private String token;
        private Map<String, String> data;
        private Webpush webpush;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Notification {
        private String title;
        private String body;
        private String icon;
    }

    @Data
    @Builder
    public static class Webpush {
        private Map<String, String> headers;
    }
}
