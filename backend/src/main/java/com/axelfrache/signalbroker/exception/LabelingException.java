package com.axelfrache.signalbroker.exception;

public class LabelingException extends RuntimeException {
    public LabelingException(String message) {
        super(message);
    }

    public LabelingException(String message, Throwable cause) {
        super(message, cause);
    }
}
