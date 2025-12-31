package com.betterlife.notify.exception;

public class FcmSendFailedException extends RuntimeException {

    public FcmSendFailedException(String message) {
        super(message);
    }

    public FcmSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
