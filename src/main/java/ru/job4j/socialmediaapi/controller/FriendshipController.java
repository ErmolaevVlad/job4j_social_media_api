package ru.job4j.socialmediaapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.job4j.socialmediaapi.controller.api.OfferFriendshipApi;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.service.FriendshipService;

@RestController
public class FriendshipController implements OfferFriendshipApi {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @Override
    public ResponseEntity<CreateOfferFriendshipResponse> createOfferFriendship(
            CreateOfferFriendshipRequest request) throws DomainException {
        var result = friendshipService.createOfferFriendship(request);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<AcceptOfferFriendshipResponse> acceptOfferFriendship(
            AcceptOfferFriendshipRequest request) throws DomainException {
        var result = friendshipService.acceptOfferFriendship(request);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<RejectOfferFriendshipsResponse> acceptOfferFriendship(
            RejectOfferFriendshipsRequest request) throws DomainException {
        var result = friendshipService.rejectOfferFriendship(request);
        return ResponseEntity.ok(result);
    }
}
