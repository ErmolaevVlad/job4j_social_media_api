package ru.job4j.socialmediaapi.exception;

public class OfferFriendshipAlreadyRejectedException extends DomainException {
    public OfferFriendshipAlreadyRejectedException(String message) {
        super(message);
    }

    public OfferFriendshipAlreadyRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
