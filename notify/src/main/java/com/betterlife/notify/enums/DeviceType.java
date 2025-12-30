package com.betterlife.notify.enums;

public enum DeviceType {
    WEB,
    ANDROID,
    IOS;

    public static DeviceType fromString(String device) {
        for (DeviceType d : values()) {
            if (d.name().equalsIgnoreCase(device)) {
                return d;
            }
        }
        throw new IllegalArgumentException("잘못된 디바이스 입니다: " + device);
    }
}
