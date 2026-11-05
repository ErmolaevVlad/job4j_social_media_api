package ru.job4j.socialmediaapi.controller;

import org.springframework.stereotype.Component;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.repository.SubscriptionRepository;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class SubscriptionPresenter {
    public CreateFollowerResponse createFollowerResponse(SubscriptionRepository.CreateFollowerResponse source) {
        String text = "Вы успешно подписались на " + source.followed_id();
        return new CreateFollowerResponse()
                .message(text);
    }

    public FindFollowersResponse findFollowersResponse(
            SubscriptionRepository.FindFollowersResponse source,
            int page,
            int limit) {
        long totalItems = source.followers().size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        if (totalPages == 0) {
            totalPages = 1;
        }

        List<FindFollowersResponseFollowersInner> followers = source.followers().stream()
                .skip((long) (page - 1) * limit)
                .limit(limit)
                .map(user -> new FindFollowersResponseFollowersInner()
                        .id(user.follower_id())
                        .username(user.follower_id().toString())
                        .followedAt(user.createdAt().atOffset(ZoneOffset.UTC))
                )
                .toList();

        var meta = new FindIncomingOfferFriendshipsResponseMeta()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page);

        return new FindFollowersResponse()
                .followers(followers)
                .meta(meta);
    }

    public FindSubscriptionsResponse findSubscriptionsResponse(
            SubscriptionRepository.FindSubscriptionsResponse source,
            int page,
            int limit) {
        long totalItems = source.subscriptions().size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        if (totalPages == 0) {
            totalPages = 1;
        }

        List<FindSubscriptionsResponseSubscriptionsInner> subscriptions = source.subscriptions().stream()
                .skip((long) (page - 1) * limit)
                .limit(limit)
                .map(user -> new FindSubscriptionsResponseSubscriptionsInner()
                        .id(user.follower_id())
                        .username(user.follower_id().toString())
                        .followedAt(user.createdAt().atOffset(ZoneOffset.UTC))
                )
                .toList();

        var meta = new FindSubscriptionsResponseMeta()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page);

        return new FindSubscriptionsResponse()
                .subscriptions(subscriptions)
                .meta(meta);
    }
}