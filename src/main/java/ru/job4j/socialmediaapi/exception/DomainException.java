package ru.job4j.socialmediaapi.exception;

public abstract class DomainException extends Exception {
    protected DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
