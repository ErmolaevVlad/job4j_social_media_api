package ru.job4j.socialmediaapi.controller.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.repository.SubscriptionRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SubscriptionsApiE2ETest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("Успех - оформление подписки на пользователя")
    void whenSubscribeToUserThenReturn200AndSaveInDb() {
        var followerId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                CreateFollowerResponse.class,
                targetUserId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedDto = new CreateFollowerResponse()
                .message("Вы успешно подписались на " + targetUserId.toString());

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto);

        var subscriptionsFound = subscriptionRepository.findSubscriptions(new SubscriptionRepository.FindUserRequest(followerId));
        assertThat(subscriptionsFound.subscriptions()).hasSize(1);
        assertThat(subscriptionsFound.subscriptions().getFirst().follower_id()).isEqualTo(targetUserId);
    }

    @Test
    @DisplayName("Статус 204 - повторная подписка на того же пользователя")
    void whenSubscribeTwiceThenReturn204() {
        var followerId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                Void.class,
                targetUserId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        var subscriptionsFound = subscriptionRepository.findSubscriptions(new SubscriptionRepository.FindUserRequest(followerId));
        assertThat(subscriptionsFound.subscriptions()).hasSize(1);
        assertThat(subscriptionsFound.subscriptions().getFirst().follower_id()).isEqualTo(targetUserId);
    }

    @Test
    @DisplayName("Успех - получение списка подписчиков")
    void whenFindFollowersThenReturnList() {
        var followerId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", targetUserId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/followers?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindFollowersResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedItem = new FindFollowersResponseFollowersInner()
                .id(followerId)
                .username(followerId.toString());

        var expectedMeta = new FindIncomingOfferFriendshipsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindFollowersResponse()
                .followers(List.of(expectedItem))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("followers.followedAt")
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Успех - получение списка подписок")
    void whenFindSubscriptionsThenReturnList() {
        var followerId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscriptions?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindSubscriptionsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedItem = new FindSubscriptionsResponseSubscriptionsInner()
                .id(targetUserId)
                .username(targetUserId.toString());

        var expectedMeta = new FindSubscriptionsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindSubscriptionsResponse()
                .subscriptions(List.of(expectedItem))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("subscriptions.followedAt")
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Успех - отписка от пользователя")
    void whenUnsubscribeThenReturn204AndDeleteFromDb() {
        var followerId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/unsubscribe/{targetUserId}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                targetUserId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        var subscriptionsFound = subscriptionRepository.findSubscriptions(new SubscriptionRepository.FindUserRequest(followerId));
        assertThat(subscriptionsFound.subscriptions()).hasSize(0);
    }

    @Test
    @DisplayName("Ошибка 409 - попытка подписаться на самого себя")
    void whenSubscribeToSelfThenReturn409() {
        var userId = UUID.randomUUID();

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                CreateFollower409Response.class,
                userId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Ошибка 404 - попытка отписаться от пользователя, на которого нет подписки")
    void whenUnsubscribeNonExistentThenReturn404() {
        var userId = UUID.randomUUID();
        var randomTargetId = UUID.randomUUID();

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/unsubscribe/{targetUserId}",
                HttpMethod.DELETE,
                entity,
                CreateFollower404Response.class,
                randomTargetId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка 400 - некорректный формат UUID при подписке")
    void whenSubscribeWithInvalidUuidThenReturn400() {
        var userId = UUID.randomUUID();

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                FindIncomingOfferFriendships400Response.class,
                "not-a-valid-uuid"
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Ошибка 400 - некорректные параметры пагинации при получении подписчиков")
    void whenFindFollowersWithInvalidPaginationThenReturn400() {
        var userId = UUID.randomUUID();
        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                "/api/subscriptions/followers?page=0&limit=101",
                HttpMethod.GET,
                entity,
                FindIncomingOfferFriendships400Response.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void subscribe(UUID followerId, UUID targetUserId) {
        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                CreateFollowerResponse.class,
                targetUserId
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}