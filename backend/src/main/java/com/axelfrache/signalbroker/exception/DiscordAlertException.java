package com.axelfrache.signalbroker.exception;

public class DiscordAlertException extends RuntimeException {
    public DiscordAlertException(String message) {
        super(message);
    }

    public DiscordAlertException(String message, Throwable cause) {
        super(message, cause);
    }
}
