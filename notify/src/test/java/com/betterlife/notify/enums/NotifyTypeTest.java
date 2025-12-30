package com.betterlife.notify.enums;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotifyTypeTest {

    @Test
    void from_recognizesReminder() {
        NotifyType type = NotifyType.from("reminder");
        assertThat(type).isEqualTo(NotifyType.REMINDER);
    }

    @Test
    void from_rejectsUnknown() {
        assertThatThrownBy(() -> NotifyType.from("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void renderTitleAndBody_replaceTokens() {
        NotifyType type = NotifyType.DEADLINE;
        String title = type.renderTitle(Map.of("title", "Report"));
        String body = type.renderBody(Map.of("title", "Report", "timeLeft", "1h"));

        assertThat(title).isEqualTo("데드라인: Report");
        assertThat(body).isEqualTo("Report 마감 1h 전입니다.");
    }
}
