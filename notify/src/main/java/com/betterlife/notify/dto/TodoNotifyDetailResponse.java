package com.betterlife.notify.dto;

import com.betterlife.notify.enums.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TodoNotifyDetailResponse {
    List<String> alarms = new ArrayList<>();

    public void addAlarm(EventType eventType, String alarm) {
        if (eventType == EventType.SCHEDULE_REMINDER) {
            alarms.add("reminder-" + alarm);
        } else {
            alarms.add("deadline-" + alarm);
        }
    }
}
