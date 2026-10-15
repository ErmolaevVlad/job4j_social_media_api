package ru.job4j.socialmediaapi.repository;

import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    CreatePostResponse createPost(CreatePostRequest request) throws DomainException;

    Optional<FindPostResponse> findById(FindPostRequest request) throws DomainException;

    FindAllActivePostsResponse findAllPublished() throws DomainException;

    UpdatePostResponse update(UpdatePostRequest request) throws DomainException;

    DeletePostResponse delete(DeletePostRequest request) throws DomainException;


    record CreatePostRequest(
            UUID id,
            UUID authorId,
            String title,
            String content,
            PostStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record CreatePostResponse(
            UUID id,
            UUID authorId,
            String title,
            String content,
            PostStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record FindPostRequest(
            UUID id
    ) {
    }

    record FindPostResponse(
            UUID id,
            UUID authorId,
            String title,
            String content,
            PostStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record FindAllActivePostsResponse(
            List<PostInfo> posts
    ) {
    }

    record PostInfo(
            UUID id,
            UUID authorId,
            String title,
            String content,
            PostStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record UpdatePostRequest(
            UUID id,
            UUID authorId,
            String title,
            String content
    ) {
    }

    record UpdatePostResponse(
            boolean isUpdated
    ) {
    }

    record DeletePostRequest(
            UUID id
    ) {
    }

    record DeletePostResponse(
            boolean isDeleted
    ) {
    }
}
