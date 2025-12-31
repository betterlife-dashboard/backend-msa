package com.betterlife.notify.exception;

public class FcmTokenDisabledException extends RuntimeException {

    public FcmTokenDisabledException(String message) {
        super(message);
    }
}
