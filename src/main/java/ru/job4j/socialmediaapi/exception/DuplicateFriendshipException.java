package ru.job4j.socialmediaapi.exception;

public class DuplicateFriendshipException extends DomainException {
    public DuplicateFriendshipException(String message) {
        super(message);
    }

    public DuplicateFriendshipException(String message, Throwable cause) {
        super(message, cause);
    }
}
