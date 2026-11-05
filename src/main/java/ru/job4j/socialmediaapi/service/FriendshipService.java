package ru.job4j.socialmediaapi.service;

import org.springframework.stereotype.Service;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.repository.FriendshipRepository;

import java.util.UUID;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;

    public FriendshipService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public FriendshipRepository.FindFriendshipsResponse findFriendships(UUID userId) throws DomainException {
        FriendshipRepository.FindFriendshipsRequest findFriendshipsRequest = new FriendshipRepository.FindFriendshipsRequest(
                userId
        );
        return friendshipRepository.findFriendships(findFriendshipsRequest);
    }

    public void deleteFriendship(UUID firstUserId, UUID secondUserId) throws DomainException {
        FriendshipRepository.DeleteFriendshipRequest deleteFriendshipRequest = new FriendshipRepository.DeleteFriendshipRequest(
                firstUserId,
                secondUserId
        );
        friendshipRepository.deleteFriendship(deleteFriendshipRequest);
    }
}