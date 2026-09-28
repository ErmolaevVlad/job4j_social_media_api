package ru.job4j.socialmediaapi.repository;

import java.time.Instant;
import java.util.UUID;

public interface OfferFriendshipRepository {
    CreateOfferFriendshipResponse createOfferFriendship(
            CreateOfferFriendshipRequest request
    );

    record CreateOfferFriendshipRequest(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record CreateOfferFriendshipResponse(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
