package ru.job4j.socialmediaapi.exception;

public class OfferFriendshipAlreadyProcessedException extends RuntimeException {
    public OfferFriendshipAlreadyProcessedException(String message) {
        super(message);
    }

    public OfferFriendshipAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }
}
