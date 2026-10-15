package ru.job4j.socialmediaapi.exception;

public class DuplicateFriendshipRequestException extends DomainException {
    public DuplicateFriendshipRequestException(String message) {
        super(message);
    }

    public DuplicateFriendshipRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
