package ru.job4j.socialmediaapi.exception;

public class SelfSubscriptionException extends DomainException {
    public SelfSubscriptionException(String message) {
        super(message);
    }
}
