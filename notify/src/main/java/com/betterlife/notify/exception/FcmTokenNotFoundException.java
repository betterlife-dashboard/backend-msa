package com.betterlife.notify.exception;

public class FcmTokenNotFoundException extends RuntimeException {

    public FcmTokenNotFoundException(String message) {
        super(message);
    }
}
