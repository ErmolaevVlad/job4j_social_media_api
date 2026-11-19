package ru.job4j.socialmediaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.socialmediaapi.controller.dto.CreatePostRequest;
import ru.job4j.socialmediaapi.repository.PostRepository;
import ru.job4j.socialmediaapi.repository.PostStatus;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public PostRepository.CreatePostResponse createPost(CreatePostRequest request) {
        PostRepository.CreatePostRequest createPostRequest =
                new PostRepository.CreatePostRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        request.getTitle(),
                        request.getContent(),
                        PostStatus.PUBLISHED,
                        Instant.now(),
                        Instant.now()
                );
        return postRepository.createPost(createPostRequest);
    }

    public void deletePost(UUID authorId, UUID postId) {
        //var post = postRepository.findById(new PostRepository.FindPostRequest(postId));
       // UUID currentUserId = currentUserProvider.getCurrentUserId();
//        if (authorId.equals(currentUserId)) {
//            throw new AccessDeniedException("Недостаточно прав для удаления чужой публикации");
//        }
        postRepository.delete(new PostRepository.DeletePostRequest(postId));

    }
}
