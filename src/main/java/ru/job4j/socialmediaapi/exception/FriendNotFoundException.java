package ru.job4j.socialmediaapi.exception;

public class FriendNotFoundException extends DomainException {
    public FriendNotFoundException(String message) {
        super(message);
    }

    public FriendNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
