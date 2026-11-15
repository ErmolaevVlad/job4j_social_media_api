package ru.job4j.socialmediaapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.job4j.socialmediaapi.controller.api.OfferFriendshipApi;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.service.OfferFriendshipService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OfferFriendshipController implements OfferFriendshipApi {
    private final OfferFriendshipService offerFriendshipService;
    private final OfferFriendshipPresenter offerFriendshipPresenter;

    @Override
    public ResponseEntity<CreateOfferFriendshipResponse> createOfferFriendship(
            CreateOfferFriendshipRequest request) {
        var result = offerFriendshipService.createOfferFriendship(request);
        var response = offerFriendshipPresenter.presentCreateOfferResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AcceptOfferFriendshipResponse> acceptOfferFriendship(
            AcceptOfferFriendshipRequest request) {
        var result = offerFriendshipService.acceptOfferFriendship(request);
        var response = offerFriendshipPresenter.presentAcceptOfferFriendshipResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RejectOfferFriendshipsResponse> rejectOfferFriendships(
            RejectOfferFriendshipsRequest request) {
        var result = offerFriendshipService.rejectOfferFriendship(request);
        var response = offerFriendshipPresenter.presentRejectOfferFriendshipResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<FindIncomingOfferFriendshipsResponse> findIncomingOfferFriendships(
            Integer page,
            Integer limit) {

        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        UUID userId = UUID.fromString(userIdHeader);
        var result = offerFriendshipService.findIncomingOfferFriendship(userId);
        var response = offerFriendshipPresenter.presentFindIncomingOfferFriendshipsResponse(result, page, limit);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<FindOutgoingOfferFriendshipsResponse> findOutgoingOfferFriendships(
            Integer page,
            Integer limit) {
        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        UUID userId = UUID.fromString(userIdHeader);
        var result = offerFriendshipService.findOutgoingOfferFriendship(userId);
        var response = offerFriendshipPresenter.presentFindOutgoingOfferFriendshipsResponse(result, page, limit);
        return ResponseEntity.ok(response);
    }
}
