package ru.job4j.socialmediaapi.repository;

import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeedRepository {

    FindFeedResponse findUserFeed(FindFeedRequest request) throws DomainException;

    record FindFeedRequest(
            UUID userId,
            int limit,
            int offset
    ) {
    }

    record FindFeedResponse(
            List<FeedPostInfo> posts
    ) {
    }

    record FeedPostInfo(
            UUID postId,
            UUID authorId,
            String title,
            String content,
            PostStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
