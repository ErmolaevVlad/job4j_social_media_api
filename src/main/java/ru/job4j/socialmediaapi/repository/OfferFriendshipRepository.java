package ru.job4j.socialmediaapi.repository;

import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OfferFriendshipRepository {
    CreateOfferFriendshipResponse createOfferFriendship(
            CreateOfferFriendshipRequest request
    ) throws DomainException;

    FindIncomingOfferFriendshipsResponse findIncomingOfferFriendships(
            FindOfferFriendshipsRequest request
    ) throws DomainException;

    FindOutgoingOfferFriendshipsResponse findOutgoingOfferFriendships(
            FindOfferFriendshipsRequest request
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

    record FindOfferFriendshipsRequest(
            UUID userId
    ) {
    }

    record FindIncomingOfferFriendshipsResponse(
            List<OfferInfo> offers
    ) {
    }

    record FindOutgoingOfferFriendshipsResponse(
            List<OfferInfo> offers
    ) {
    }

    record OfferInfo(
            UUID userId,
            Instant createdAt
    ) {
    }
}
