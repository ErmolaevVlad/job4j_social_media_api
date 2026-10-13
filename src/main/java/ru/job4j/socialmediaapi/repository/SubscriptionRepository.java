package ru.job4j.socialmediaapi.repository;

import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository {

    FindFollowersResponse findFollowers(FindUserRequest request) throws DomainException;

    FindSubscriptionsResponse findSubscriptions(FindUserRequest request) throws DomainException;

    CreateFollowerResponse createFollower(CreateFollowerRequest requestCreate) throws DomainException;

    record CreateFollowerRequest(
            UUID id,
            UUID followed_id,
            UUID follower_id,
            Instant createdAt
    ) {
    }

    record CreateFollowerResponse(
            UUID id,
            UUID followed_id,
            UUID follower_id,
            Instant createdAt
    ) {
    }

    record FindUserRequest(
            UUID userId
    ) {
    }

    record FindFollowersResponse(
            List<UserInfo> followers
    ) {
    }

    record UserInfo(
            UUID follower_id,
            Instant createdAt
    ) {
    }

    record FindSubscriptionsResponse(
            List<UserInfo> subscriptions
    ) {
    }
}
