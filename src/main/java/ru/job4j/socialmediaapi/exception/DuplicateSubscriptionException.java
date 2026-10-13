package ru.job4j.socialmediaapi.exception;

public class DuplicateSubscriptionException extends DomainException {
    public DuplicateSubscriptionException(String message) {
        super(message);
    }
}
