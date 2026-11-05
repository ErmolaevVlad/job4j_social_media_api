package ru.job4j.socialmediaapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.job4j.socialmediaapi.controller.api.SubscriptionsApi;
import ru.job4j.socialmediaapi.controller.dto.CreateFollowerResponse;
import ru.job4j.socialmediaapi.controller.dto.FindFollowersResponse;
import ru.job4j.socialmediaapi.controller.dto.FindSubscriptionsResponse;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.service.SubscriptionService;

import java.util.UUID;

@RestController
public class SubscriptionController implements SubscriptionsApi {
    private final SubscriptionService subscriptionService;
    private final SubscriptionPresenter subscriptionPresenter;

    public SubscriptionController(SubscriptionService subscriptionService, SubscriptionPresenter subscriptionPresenter) {
        this.subscriptionService = subscriptionService;
        this.subscriptionPresenter = subscriptionPresenter;
    }

    @Override
    public ResponseEntity<CreateFollowerResponse> createFollower(@PathVariable("targetUserId") UUID targetUserId) throws DomainException {

        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        UUID follower = UUID.fromString(userIdHeader);
        var result = subscriptionService.createFollower(follower, targetUserId);
        var response = subscriptionPresenter.createFollowerResponse(result);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteSubscription(@PathVariable("targetUserId") UUID targetUserId) throws DomainException {
        UUID follower = extractUserId();
        subscriptionService.deleteSubscription(follower, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<FindFollowersResponse> findFollowers(Integer page, Integer limit) throws DomainException {
        UUID userId = extractUserId();
        var result = subscriptionService.findFollowers(userId);
        var response = subscriptionPresenter.findFollowersResponse(result, page, limit);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<FindSubscriptionsResponse> findSubscriptions(Integer page, Integer limit) throws DomainException {
        UUID userId = extractUserId();
        var result = subscriptionService.findSubscriptions(userId);
        var response = subscriptionPresenter.findSubscriptionsResponse(result, page, limit);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserId() {
        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String userIdHeader = servletRequest.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-User-Id header");
        }
        return UUID.fromString(userIdHeader);
    }
}
