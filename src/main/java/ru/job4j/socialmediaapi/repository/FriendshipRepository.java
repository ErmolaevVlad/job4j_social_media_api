package ru.job4j.socialmediaapi.repository;

import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FriendshipRepository {

    CreateFriendshipResponse createFriendship(CreateFriendshipRequest requestCreate)
            throws DomainException;

    FindFriendshipsResponse findFriendships(FindFriendshipsRequest request) throws DomainException;

    record CreateFriendshipRequest(
            UUID firstUserId,
            UUID secondUserId,
            UUID offerFriendshipId,
            Instant createdAt
    ) {
    }

    record CreateFriendshipResponse(
            UUID id,
            UUID firstUserId,
            UUID secondUserId,
            UUID offerFriendshipId,
            Instant createdAt
    ) {
    }

    record FindFriendshipsRequest(
            UUID userId
    ) {
    }

    record FindFriendshipsResponse(
            List<FriendshipInfo> friendships
    ) {
    }

    record FriendshipInfo(
            UUID friendId,
            Instant createdAt
    ) {
    }
}
