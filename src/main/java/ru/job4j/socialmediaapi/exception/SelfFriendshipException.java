package ru.job4j.socialmediaapi.exception;

public class SelfFriendshipException extends DomainException {
    public SelfFriendshipException(String message) {
        super(message);
    }

    public SelfFriendshipException(String message, Throwable cause) {
        super(message, cause);
    }
}
