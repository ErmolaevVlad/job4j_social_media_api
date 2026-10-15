package ru.job4j.socialmediaapi.exception;

public class SubscriptionNotFoundException extends DomainException {
    public SubscriptionNotFoundException(String message) {
        super(message);
    }

    public SubscriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

