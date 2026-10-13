package ru.job4j.socialmediaapi.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.exception.DuplicateSubscriptionException;
import ru.job4j.socialmediaapi.exception.SelfSubscriptionException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@Transactional
@SpringBootTest
class JdbcSubscriptionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private static final UUID FOLLOWER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FOLLOWED = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant FIXED_NOW = Instant.parse("2026-09-22T10:15:00Z");

    @Test
    @DisplayName("Успех — создание подписки")
    void whenCreateOfferFriendshipThenReturnStoredData() throws DomainException {
        var id = UUID.randomUUID();
        var request = new SubscriptionRepository.CreateFollowerRequest(
                id,
                FOLLOWER,
                FOLLOWED,
                FIXED_NOW
        );
        var resultCreate = subscriptionRepository.createFollower(request);
        var expected = new SubscriptionRepository.CreateFollowerResponse(
                id,
                FOLLOWER,
                FOLLOWED,
                FIXED_NOW
        );
        assertThat(resultCreate).isEqualTo(expected);
    }

    @Test
    @DisplayName("Успех — получение списка подписчиков")
    void whenCreateOfferFriendshipThenReturnFindFollowers() throws DomainException {
        var id = UUID.randomUUID();
        createSubscription(id, FOLLOWER, FOLLOWED);
        var user = new SubscriptionRepository.FindUserRequest(FOLLOWED);
        var rsl = subscriptionRepository.findFollowers(user);
        var expected = new SubscriptionRepository.FindFollowersResponse(
                List.of(new SubscriptionRepository.UserInfo(FOLLOWER, FIXED_NOW)));
        assertThat(rsl).isEqualTo(expected);
    }

    @Test
    @DisplayName("Успех — получение списка подписок")
    void whenCreateOfferFriendshipThenReturnFindSubscriptions() throws DomainException {
        var id = UUID.randomUUID();
        createSubscription(id, FOLLOWER, FOLLOWED);
        var user = new SubscriptionRepository.FindUserRequest(FOLLOWER);
        var rsl = subscriptionRepository.findSubscriptions(user);
        var expected = new SubscriptionRepository.FindSubscriptionsResponse(
                List.of(new SubscriptionRepository.UserInfo(FOLLOWED, FIXED_NOW)));
        assertThat(rsl).isEqualTo(expected);
    }

    @Test
    @DisplayName("Успех — получение исключения, если пользователь пытается подписаться на себя")
    void shouldThrowExceptionWhenUserSubscribesToThemselves() {
        var id = UUID.randomUUID();
        var request = new SubscriptionRepository.CreateFollowerRequest(
                id,
                FOLLOWER,
                FOLLOWER,
                FIXED_NOW
        );
        assertThatThrownBy(() -> subscriptionRepository.createFollower(request))
                .isInstanceOf(SelfSubscriptionException.class);
    }

    @Test
    @DisplayName("Успех — получение исключения, если пользователь пытается подписаться еще раз")
    void shouldThrowExceptionWhenDuplicateSubscriptionIsCreated() throws DomainException {
        var id = UUID.randomUUID();
        createSubscription(id, FOLLOWER, FOLLOWED);
        var secondRequest = new SubscriptionRepository.CreateFollowerRequest(
                UUID.randomUUID(),
                FOLLOWER,
                FOLLOWED,
                FIXED_NOW
        );

        assertThatThrownBy(() -> subscriptionRepository.createFollower(secondRequest))
                .isInstanceOf(DuplicateSubscriptionException.class);
    }

    private void createSubscription(UUID id, UUID follower, UUID followed) throws DomainException {
        var request = new SubscriptionRepository.CreateFollowerRequest(id, follower, followed, FIXED_NOW);
        subscriptionRepository.createFollower(request);
    }
}