package ru.job4j.socialmediaapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.job4j.socialmediaapi.controller.api.FriendshipsApi;
import ru.job4j.socialmediaapi.controller.dto.FindFriendshipsResponse;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.service.FriendshipService;

import java.util.UUID;

@RestController
public class FriendshipController implements FriendshipsApi {
    private final FriendshipService friendshipService;
    private final FriendshipPresenter friendshipPresenter;

    public FriendshipController(FriendshipService friendshipService, FriendshipPresenter friendshipPresenter) {
        this.friendshipService = friendshipService;
        this.friendshipPresenter = friendshipPresenter;
    }

    @Override
    public ResponseEntity<FindFriendshipsResponse> findFriendships(
            Integer page,
            Integer limit) throws DomainException {

        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        UUID userId = UUID.fromString(userIdHeader);
        var result = friendshipService.findFriendships(userId);
        var response = friendshipPresenter.findFriendshipResponsePresent(page, limit, result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteFriend(@PathVariable("friendId") UUID secondUserId) throws DomainException {

        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        UUID firstUserId = UUID.fromString(userIdHeader);
        friendshipService.deleteFriendship(firstUserId, secondUserId);
        return ResponseEntity.noContent().build();
    }
}
