package ru.job4j.socialmediaapi.exception;

public class OfferFriendshipNotFoundException extends RuntimeException {
    public OfferFriendshipNotFoundException(String message) {
        super(message);
    }

    public OfferFriendshipNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
