package ru.job4j.socialmediaapi.exception;

public class DatabaseWriteException extends DomainException {
    public DatabaseWriteException(String message) {
        super(message);
    }

    public DatabaseWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
