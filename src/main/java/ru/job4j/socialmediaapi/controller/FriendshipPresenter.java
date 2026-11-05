package ru.job4j.socialmediaapi.controller;

import org.springframework.stereotype.Component;
import ru.job4j.socialmediaapi.controller.dto.FindFriendshipsResponse;
import ru.job4j.socialmediaapi.controller.dto.FindFriendshipsResponseFriendsInner;
import ru.job4j.socialmediaapi.controller.dto.FindFriendshipsResponseMeta;
import ru.job4j.socialmediaapi.repository.FriendshipRepository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class FriendshipPresenter {

    public FindFriendshipsResponse findFriendshipResponsePresent(
            int page,
            int limit,
            FriendshipRepository.FindFriendshipsResponse source) {
        List<FindFriendshipsResponseFriendsInner> response =
                source.friendships().stream().map(friend -> new FindFriendshipsResponseFriendsInner()
                        .id(UUID.randomUUID())
                        .username(friend.friendId().toString())
                        .friendsSince(friend.createdAt().atOffset(ZoneOffset.UTC))
                        )
                        .toList();
        long totalItems = response.size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        if (totalPages == 0) {
            totalPages = 1;
        }

        var meta = new FindFriendshipsResponseMeta()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page);
        return new FindFriendshipsResponse()
                .friends(response)
                .meta(meta);
    }
}
