package ru.job4j.socialmediaapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.job4j.socialmediaapi.controller.api.OfferFriendshipApi;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.service.OfferFriendshipService;

import java.util.UUID;

@RestController
public class OfferFriendshipController implements OfferFriendshipApi {
    private final OfferFriendshipService offerFriendshipService;
    private final OfferFriendshipPresenter offerFriendshipPresenter;

    public OfferFriendshipController(OfferFriendshipService offerFriendshipService, OfferFriendshipPresenter offerFriendshipPresenter) {
        this.offerFriendshipService = offerFriendshipService;
        this.offerFriendshipPresenter = offerFriendshipPresenter;
    }

    @Override
    public ResponseEntity<CreateOfferFriendshipResponse> createOfferFriendship(
            CreateOfferFriendshipRequest request) throws DomainException {
        var result = offerFriendshipService.createOfferFriendship(request);
        var response = offerFriendshipPresenter.presentCreateOfferResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AcceptOfferFriendshipResponse> acceptOfferFriendship(
            AcceptOfferFriendshipRequest request) throws DomainException {
        var result = offerFriendshipService.acceptOfferFriendship(request);
        var response = offerFriendshipPresenter.presentAcceptOfferFriendshipResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RejectOfferFriendshipsResponse> rejectOfferFriendships(
            RejectOfferFriendshipsRequest request) throws DomainException {
        var result = offerFriendshipService.rejectOfferFriendship(request);
        var response = offerFriendshipPresenter.presentRejectOfferFriendshipResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<FindIncomingOfferFriendshipsResponse> findIncomingOfferFriendships(
            Integer page,
            Integer limit) throws DomainException {

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
            Integer limit) throws DomainException {
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
