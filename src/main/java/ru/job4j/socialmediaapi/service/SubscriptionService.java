package ru.job4j.socialmediaapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.repository.SubscriptionRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public SubscriptionRepository.CreateFollowerResponse createFollower(UUID follower, UUID targetUserId) throws DomainException {
        SubscriptionRepository.CreateFollowerRequest createFollowerRequest = new SubscriptionRepository.CreateFollowerRequest(
                UUID.randomUUID(),
                follower,
                targetUserId,
                Instant.now()
        );
        return subscriptionRepository.createFollower(createFollowerRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSubscription(UUID follower, UUID targetUserId) throws DomainException {
        var request = new SubscriptionRepository.DeleteSubscriptionRequest(follower, targetUserId);
        subscriptionRepository.deleteSubscription(request);
    }

    @Transactional(readOnly = true)
    public SubscriptionRepository.FindFollowersResponse findFollowers(UUID userId) throws DomainException {
        return subscriptionRepository.findFollowers(new SubscriptionRepository.FindUserRequest(userId));
    }

    @Transactional(readOnly = true)
    public SubscriptionRepository.FindSubscriptionsResponse findSubscriptions(UUID userId) throws DomainException {
        return subscriptionRepository.findSubscriptions(new SubscriptionRepository.FindUserRequest(userId));
    }
}