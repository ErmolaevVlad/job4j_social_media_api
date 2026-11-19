package ru.job4j.socialmediaapi.controller;

import org.springframework.stereotype.Component;
import ru.job4j.socialmediaapi.controller.dto.CreatePostResponse;
import ru.job4j.socialmediaapi.repository.PostRepository;

import java.time.ZoneOffset;

@Component
public class PostPresenter {
    public CreatePostResponse presentCreatePostResponse(
            PostRepository.CreatePostResponse source) {
        return new CreatePostResponse()
                .id(source.id())
                .authorId(source.authorId())
                .title(source.title())
                .content(source.content())
                .status(CreatePostResponse.StatusEnum.valueOf(source.status().name()))
                .createdAt(source.createdAt().atOffset(ZoneOffset.UTC))
                .updatedAt(source.updatedAt().atOffset(ZoneOffset.UTC));
    }
}
