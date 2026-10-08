package ru.job4j.socialmediaapi.repository;

import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.UUID;

public interface OfferFriendshipRepository {
    CreateOfferFriendshipResponse createOfferFriendship(
            CreateOfferFriendshipRequest request
    ) throws DomainException;

    record CreateOfferFriendshipRequest(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            FriendshipStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record CreateOfferFriendshipResponse(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            FriendshipStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
