package ru.job4j.socialmediaapi.exception;

public class OfferFriendshipAlreadyAcceptException extends DomainException {
    public OfferFriendshipAlreadyAcceptException(String message) {
        super(message);
    }

    public OfferFriendshipAlreadyAcceptException(String message, Throwable cause) {
        super(message, cause);
    }
}
