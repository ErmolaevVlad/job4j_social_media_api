package ru.job4j.socialmediaapi.exception;

public class DatabaseReadException extends DomainException {
    public DatabaseReadException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
