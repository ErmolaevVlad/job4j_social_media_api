package ru.job4j.socialmediaapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.job4j.socialmediaapi.controller.api.PostsApi;
import ru.job4j.socialmediaapi.controller.dto.CreatePostRequest;
import ru.job4j.socialmediaapi.controller.dto.CreatePostResponse;
import ru.job4j.socialmediaapi.service.PostService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PostController implements PostsApi {
    private final PostService postService;
    private final PostPresenter postPresenter;

    @Override
    public ResponseEntity<CreatePostResponse> createPost(CreatePostRequest request) {
        var result = postService.createPost(request);
        var response = postPresenter.presentCreatePostResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deletePost(@PathVariable("postId") UUID postId) {
        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        UUID authorId = UUID.fromString(userIdHeader);
        postService.deletePost(authorId, postId);
        return ResponseEntity.noContent().build();
    }
}
